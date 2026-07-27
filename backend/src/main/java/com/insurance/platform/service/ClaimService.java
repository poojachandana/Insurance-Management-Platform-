package com.insurance.platform.service;

import com.insurance.platform.dto.ClaimDecisionRequest;
import com.insurance.platform.dto.ClaimRequest;
import com.insurance.platform.dto.ClaimResponse;
import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.entity.ClaimStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClaimService {
    ClaimResponse submit(ClaimRequest request, String actorEmail);
    ClaimResponse assign(Long claimId, Long agentId);
    ClaimResponse attachDocuments(Long claimId, List<Long> documentIds, String actorEmail);
    ClaimResponse decide(Long claimId, ClaimDecisionRequest request, String agentEmail, String requestingAgentEmail);
    ClaimResponse markUnderReview(Long claimId, String requestingAgentEmail);

    /** requestingAgentEmail: null for Admins (unrestricted), or the caller's email — Agents see claims
     *  for customers they registered, plus any claim explicitly assigned to them by an Admin. */
    ClaimResponse getById(Long id, String requestingAgentEmail);
    List<ClaimResponse> getAll(String requestingAgentEmail);
    List<ClaimResponse> getByPolicy(Long policyId, String requestingAgentEmail);
    List<ClaimResponse> getByCurrentCustomerUser(String email);
    List<ClaimResponse> getPending();
    List<ClaimResponse> getAssignedToAgent(String agentEmail);
    PageResponse<ClaimResponse> filterPaged(ClaimStatus status, String search, String requestingAgentEmail, Pageable pageable);
}
