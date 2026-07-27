package com.insurance.platform.service;

import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.dto.PolicyRequest;
import com.insurance.platform.dto.PolicyResponse;
import com.insurance.platform.entity.PolicyStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PolicyService {
    PolicyResponse create(PolicyRequest request, String agentEmail);
    PolicyResponse update(Long id, PolicyRequest request, String actorEmail, String requestingAgentEmail);
    PolicyResponse renew(Long id, int extendMonths, String actorEmail, String requestingAgentEmail);
    PolicyResponse cancel(Long id, String actorEmail, String requestingAgentEmail);

    /** requestingAgentEmail: null for Admins (unrestricted), or the caller's email to scope to customers they registered. */
    PolicyResponse getById(Long id, String requestingAgentEmail);
    List<PolicyResponse> getAll(String requestingAgentEmail);
    List<PolicyResponse> getByCustomer(Long customerId, String requestingAgentEmail);
    List<PolicyResponse> getByCurrentCustomerUser(String email);
    List<PolicyResponse> getExpiringWithinDays(int days);
    PageResponse<PolicyResponse> filterPaged(PolicyStatus status, String search, String requestingAgentEmail, Pageable pageable);
    void checkAndUpdateExpiredPolicies();
    void checkAndNotifyExpiringPolicies();
}
