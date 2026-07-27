package com.insurance.platform.service;

import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.dto.PremiumPaymentRequest;
import com.insurance.platform.dto.PremiumPaymentResponse;
import com.insurance.platform.entity.PaymentStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PremiumPaymentService {
    PremiumPaymentResponse createDue(PremiumPaymentRequest request, String actorEmail, String requestingAgentEmail);
    PremiumPaymentResponse recordPayment(Long id, String actorEmail, String requestingAgentEmail);

    /** requestingAgentEmail: null for Admins (unrestricted), or the caller's email to scope to customers they registered. */
    List<PremiumPaymentResponse> getByPolicy(Long policyId, String requestingAgentEmail);
    List<PremiumPaymentResponse> getByCurrentCustomerUser(String email);
    List<PremiumPaymentResponse> getAll(String requestingAgentEmail);
    List<PremiumPaymentResponse> getOverdue();
    PageResponse<PremiumPaymentResponse> filterPaged(PaymentStatus status, String search, String requestingAgentEmail, Pageable pageable);
    void refreshOverdueStatuses();
}
