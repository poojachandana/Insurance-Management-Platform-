package com.insurance.platform.service.impl;

import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.dto.PremiumPaymentRequest;
import com.insurance.platform.dto.PremiumPaymentResponse;
import com.insurance.platform.entity.NotificationType;
import com.insurance.platform.entity.PaymentStatus;
import com.insurance.platform.entity.Policy;
import com.insurance.platform.entity.PremiumPayment;
import com.insurance.platform.entity.Customer;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.repository.CustomerRepository;
import com.insurance.platform.repository.PolicyRepository;
import com.insurance.platform.repository.PremiumPaymentRepository;
import com.insurance.platform.service.AuditLogService;
import com.insurance.platform.service.NotificationService;
import com.insurance.platform.service.PremiumPaymentService;
import com.insurance.platform.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PremiumPaymentServiceImpl implements PremiumPaymentService {

    private final PremiumPaymentRepository premiumPaymentRepository;
    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final SystemSettingsService systemSettingsService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public PremiumPaymentResponse createDue(PremiumPaymentRequest request, String actorEmail, String requestingAgentEmail) {
        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + request.getPolicyId()));
        assertOwnership(policy, requestingAgentEmail);

        PremiumPayment payment = PremiumPayment.builder()
                .policy(policy)
                .dueDate(request.getDueDate())
                .amount(request.getAmount())
                .paymentStatus(PaymentStatus.DUE)
                .build();

        PremiumPayment saved = premiumPaymentRepository.save(payment);
        auditLogService.log(actorEmail, "CREATE", "PREMIUM", saved.getId(),
                "Scheduled premium of " + saved.getAmount() + " due " + saved.getDueDate() + " for policy " + policy.getPolicyNumber());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PremiumPaymentResponse recordPayment(Long id, String actorEmail, String requestingAgentEmail) {
        PremiumPayment payment = premiumPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Premium payment not found with id: " + id));
        assertOwnership(payment.getPolicy(), requestingAgentEmail);
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaymentDate(LocalDate.now());
        PremiumPayment saved = premiumPaymentRepository.save(payment);
        auditLogService.log(actorEmail, "PAY", "PREMIUM", saved.getId(),
                "Premium of " + saved.getAmount() + " for policy " + saved.getPolicy().getPolicyNumber() + " marked as paid");
        return toResponse(saved);
    }

    @Override
    public List<PremiumPaymentResponse> getByPolicy(Long policyId, String requestingAgentEmail) {
        List<PremiumPayment> payments = premiumPaymentRepository.findByPolicyId(policyId);
        if (!payments.isEmpty()) {
            assertOwnership(payments.get(0).getPolicy(), requestingAgentEmail);
        }
        return payments.stream().map(this::toResponse).toList();
    }

    @Override
    public List<PremiumPaymentResponse> getByCurrentCustomerUser(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for current user"));
        return premiumPaymentRepository.findByPolicy_Customer_Id(customer.getId()).stream().map(this::toResponse).toList();
    }

    @Override
    public List<PremiumPaymentResponse> getAll(String requestingAgentEmail) {
        return premiumPaymentRepository.findScoped(requestingAgentEmail).stream().map(this::toResponse).toList();
    }

    @Override
    public List<PremiumPaymentResponse> getOverdue() {
        return premiumPaymentRepository.findByPaymentStatus(PaymentStatus.OVERDUE).stream().map(this::toResponse).toList();
    }

    @Override
    public PageResponse<PremiumPaymentResponse> filterPaged(PaymentStatus status, String search, String requestingAgentEmail, Pageable pageable) {
        Page<PremiumPayment> page = premiumPaymentRepository.filter(requestingAgentEmail, status,
                (search == null || search.isBlank()) ? null : search, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    /**
     * Runs daily to mark any DUE premium whose due date + grace period has passed
     * as OVERDUE, and raises an in-app alert for the customer (Overdue Premium
     * Alerts support). The grace period is configurable via System Settings.
     */
    @Override
    @Scheduled(cron = "0 30 0 * * *")
    @Transactional
    public void refreshOverdueStatuses() {
        int graceDays = systemSettingsService.getOrCreateDefault().getPremiumGraceDays();
        LocalDate cutoff = LocalDate.now().minusDays(graceDays);

        List<PremiumPayment> overdueCandidates = premiumPaymentRepository
                .findByDueDateBeforeAndPaymentStatusNot(cutoff, PaymentStatus.PAID);

        for (PremiumPayment payment : overdueCandidates) {
            if (payment.getPaymentStatus() != PaymentStatus.OVERDUE) {
                payment.setPaymentStatus(PaymentStatus.OVERDUE);
                premiumPaymentRepository.save(payment);

                String message = String.format(
                        "Your premium payment of %s for policy %s was due on %s and is now overdue. Please pay as soon as possible.",
                        payment.getAmount(), payment.getPolicy().getPolicyNumber(), payment.getDueDate());

                notificationService.notifyOnce(
                        payment.getPolicy().getCustomer().getEmail(),
                        "Premium Payment Overdue",
                        message,
                        NotificationType.PREMIUM_OVERDUE,
                        payment.getId());

                auditLogService.log("SYSTEM", "AUTO_OVERDUE", "PREMIUM", payment.getId(),
                        "Premium for policy " + payment.getPolicy().getPolicyNumber() + " automatically marked OVERDUE");
            }
        }
    }

    /** Throws if an Agent (requestingAgentEmail != null) tries to access a premium for a customer they didn't register. Admins (null) bypass this. */
    private void assertOwnership(Policy policy, String requestingAgentEmail) {
        if (requestingAgentEmail == null) {
            return; // Admin — unrestricted
        }
        boolean owns = requestingAgentEmail.equalsIgnoreCase(policy.getCustomer().getRegisteredByAgentEmail());
        if (!owns) {
            throw new AccessDeniedException("You can only access premiums for customers you registered");
        }
    }

    private PremiumPaymentResponse toResponse(PremiumPayment payment) {
        return PremiumPaymentResponse.builder()
                .id(payment.getId())
                .policyId(payment.getPolicy().getId())
                .policyNumber(payment.getPolicy().getPolicyNumber())
                .customerName(payment.getPolicy().getCustomer().getName())
                .paymentDate(payment.getPaymentDate())
                .dueDate(payment.getDueDate())
                .amount(payment.getAmount())
                .paymentStatus(payment.getPaymentStatus())
                .build();
    }
}
