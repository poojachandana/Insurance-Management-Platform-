package com.insurance.platform.service.impl;

import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.dto.PolicyRequest;
import com.insurance.platform.dto.PolicyResponse;
import com.insurance.platform.entity.Customer;
import com.insurance.platform.entity.NotificationType;
import com.insurance.platform.entity.Policy;
import com.insurance.platform.entity.PolicyCategory;
import com.insurance.platform.entity.PolicyStatus;
import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.repository.CustomerRepository;
import com.insurance.platform.repository.PolicyCategoryRepository;
import com.insurance.platform.repository.PolicyRepository;
import com.insurance.platform.service.AuditLogService;
import com.insurance.platform.service.NotificationService;
import com.insurance.platform.service.PolicyService;
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
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;
    private final PolicyCategoryRepository policyCategoryRepository;
    private final NotificationService notificationService;
    private final SystemSettingsService systemSettingsService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public PolicyResponse create(PolicyRequest request, String agentEmail) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
        PolicyCategory category = policyCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy category not found with id: " + request.getCategoryId()));

        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        Policy policy = Policy.builder()
                .customer(customer)
                .category(category)
                .policyType(category.getName())
                .premiumAmount(request.getPremiumAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(PolicyStatus.ACTIVE)
                .createdByAgentEmail(agentEmail)
                .build();

        policy = policyRepository.save(policy);
        auditLogService.log(agentEmail, "CREATE", "POLICY", policy.getId(),
                "Created " + category.getName() + " policy " + policy.getPolicyNumber() + " for " + customer.getName());
        return toResponse(policy);
    }

    @Override
    @Transactional
    public PolicyResponse update(Long id, PolicyRequest request, String actorEmail, String requestingAgentEmail) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));
        assertOwnership(policy, requestingAgentEmail);

        if (request.getCustomerId() != null && !request.getCustomerId().equals(policy.getCustomer().getId())) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
            policy.setCustomer(customer);
        }
        if (request.getCategoryId() != null) {
            PolicyCategory category = policyCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Policy category not found with id: " + request.getCategoryId()));
            policy.setCategory(category);
            policy.setPolicyType(category.getName());
        }
        policy.setPremiumAmount(request.getPremiumAmount());
        policy.setStartDate(request.getStartDate());
        policy.setEndDate(request.getEndDate());

        Policy saved = policyRepository.save(policy);
        auditLogService.log(actorEmail, "UPDATE", "POLICY", saved.getId(), "Updated policy " + saved.getPolicyNumber());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PolicyResponse renew(Long id, int extendMonths, String actorEmail, String requestingAgentEmail) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));
        assertOwnership(policy, requestingAgentEmail);
        LocalDate base = policy.getEndDate().isAfter(LocalDate.now()) ? policy.getEndDate() : LocalDate.now();
        policy.setEndDate(base.plusMonths(extendMonths));
        policy.setStatus(PolicyStatus.ACTIVE);
        Policy saved = policyRepository.save(policy);
        auditLogService.log(actorEmail, "RENEW", "POLICY", saved.getId(),
                "Renewed policy " + saved.getPolicyNumber() + " by " + extendMonths + " month(s), new end date " + saved.getEndDate());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PolicyResponse cancel(Long id, String actorEmail, String requestingAgentEmail) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));
        assertOwnership(policy, requestingAgentEmail);
        policy.setStatus(PolicyStatus.CANCELLED);
        Policy saved = policyRepository.save(policy);
        auditLogService.log(actorEmail, "CANCEL", "POLICY", saved.getId(), "Cancelled policy " + saved.getPolicyNumber());
        return toResponse(saved);
    }

    @Override
    public PolicyResponse getById(Long id, String requestingAgentEmail) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + id));
        assertOwnership(policy, requestingAgentEmail);
        return toResponse(policy);
    }

    @Override
    public List<PolicyResponse> getAll(String requestingAgentEmail) {
        return policyRepository.findScoped(requestingAgentEmail).stream().map(this::toResponse).toList();
    }

    @Override
    public List<PolicyResponse> getByCustomer(Long customerId, String requestingAgentEmail) {
        List<Policy> policies = policyRepository.findByCustomerId(customerId);
        if (!policies.isEmpty()) {
            assertOwnership(policies.get(0), requestingAgentEmail);
        } else if (requestingAgentEmail != null) {
            // Even with no policies yet, confirm the agent actually owns this customer before returning an empty (but valid) list.
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
            if (!requestingAgentEmail.equalsIgnoreCase(customer.getRegisteredByAgentEmail())) {
                throw new AccessDeniedException("You can only view policies for customers you registered");
            }
        }
        return policies.stream().map(this::toResponse).toList();
    }

    @Override
    public List<PolicyResponse> getByCurrentCustomerUser(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for current user"));
        return policyRepository.findByCustomerId(customer.getId()).stream().map(this::toResponse).toList();
    }

    @Override
    public List<PolicyResponse> getExpiringWithinDays(int days) {
        LocalDate today = LocalDate.now();
        return policyRepository.findByEndDateBetweenAndStatus(today, today.plusDays(days), PolicyStatus.ACTIVE)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public PageResponse<PolicyResponse> filterPaged(PolicyStatus status, String search, String requestingAgentEmail, Pageable pageable) {
        Page<Policy> page = policyRepository.filter(requestingAgentEmail, status,
                (search == null || search.isBlank()) ? null : search, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    /**
     * Runs once every day at midnight to automatically flip policies whose
     * end date has passed from ACTIVE to EXPIRED (Policy Expiry Notification support).
     */
    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkAndUpdateExpiredPolicies() {
        List<Policy> activePolicies = policyRepository.findByStatus(PolicyStatus.ACTIVE);
        LocalDate today = LocalDate.now();
        for (Policy policy : activePolicies) {
            if (policy.getEndDate().isBefore(today)) {
                policy.setStatus(PolicyStatus.EXPIRED);
                policyRepository.save(policy);
                auditLogService.log("SYSTEM", "AUTO_EXPIRE", "POLICY", policy.getId(),
                        "Policy " + policy.getPolicyNumber() + " automatically marked EXPIRED");
            }
        }
    }

    /**
     * Runs once every day at 06:00 to raise a POLICY_EXPIRY notification for
     * policies expiring within the configured reminder window (Policy Expiry
     * Notifications support). Notifies both the customer and the agent who
     * created the policy, and only once per policy (notifyOnce).
     */
    @Override
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void checkAndNotifyExpiringPolicies() {
        int reminderDays = systemSettingsService.getOrCreateDefault().getPolicyExpiryReminderDays();
        LocalDate today = LocalDate.now();
        List<Policy> expiringSoon = policyRepository.findByEndDateBetweenAndStatus(
                today, today.plusDays(reminderDays), PolicyStatus.ACTIVE);

        for (Policy policy : expiringSoon) {
            String message = String.format(
                    "Policy %s (%s) is expiring on %s. Please renew to avoid a lapse in coverage.",
                    policy.getPolicyNumber(), policy.getPolicyType(), policy.getEndDate());

            notificationService.notifyOnce(
                    policy.getCustomer().getEmail(),
                    "Policy Expiring Soon",
                    message,
                    NotificationType.POLICY_EXPIRY,
                    policy.getId());

            if (policy.getCreatedByAgentEmail() != null) {
                notificationService.notifyOnce(
                        policy.getCreatedByAgentEmail(),
                        "Policy Expiring Soon",
                        message,
                        NotificationType.POLICY_EXPIRY,
                        policy.getId());
            }
        }
    }

    /** Throws if an Agent (requestingAgentEmail != null) tries to access a policy for a customer they didn't register. Admins (null) bypass this. */
    private void assertOwnership(Policy policy, String requestingAgentEmail) {
        if (requestingAgentEmail == null) {
            return; // Admin — unrestricted
        }
        boolean owns = requestingAgentEmail.equalsIgnoreCase(policy.getCustomer().getRegisteredByAgentEmail());
        if (!owns) {
            throw new AccessDeniedException("You can only access policies for customers you registered");
        }
    }

    private PolicyResponse toResponse(Policy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .customerId(policy.getCustomer().getId())
                .customerName(policy.getCustomer().getName())
                .policyType(policy.getPolicyType())
                .categoryId(policy.getCategory() != null ? policy.getCategory().getId() : null)
                .categoryName(policy.getCategory() != null ? policy.getCategory().getName() : policy.getPolicyType())
                .policyNumber(policy.getPolicyNumber())
                .premiumAmount(policy.getPremiumAmount())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .status(policy.getStatus())
                .build();
    }
}
