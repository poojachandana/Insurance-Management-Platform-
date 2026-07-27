package com.insurance.platform.service;

import com.insurance.platform.dto.CustomerRequest;
import com.insurance.platform.dto.CustomerResponse;
import com.insurance.platform.dto.CustomerSelfUpdateRequest;
import com.insurance.platform.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {
    CustomerResponse create(CustomerRequest request, String actorEmail);
    CustomerResponse update(Long id, CustomerRequest request, String actorEmail, String requestingAgentEmail);
    CustomerResponse updateOwnProfile(String email, CustomerSelfUpdateRequest request);

    CustomerResponse getById(Long id, String requestingAgentEmail);
    CustomerResponse getByCurrentUser(String email);

    List<CustomerResponse> getAll(String requestingAgentEmail);
    List<CustomerResponse> search(String keyword, String requestingAgentEmail);
    PageResponse<CustomerResponse> getAllPaged(String requestingAgentEmail, Pageable pageable);
    PageResponse<CustomerResponse> searchPaged(String keyword, String requestingAgentEmail, Pageable pageable);

    void delete(Long id, String actorEmail);

    /** Admin-only: assigns (or unassigns, if agentId is null) which Agent manages this customer —
     *  used to route self-registered customers, who start with no Agent assigned. */
    CustomerResponse assignAgent(Long customerId, Long agentId, String actorEmail);
}