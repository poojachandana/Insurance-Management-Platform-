package com.insurance.platform.service.impl;

import com.insurance.platform.dto.ClaimDecisionRequest;
import com.insurance.platform.dto.ClaimDocumentResponse;
import com.insurance.platform.dto.ClaimRequest;
import com.insurance.platform.dto.ClaimResponse;
import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.entity.Claim;
import com.insurance.platform.entity.ClaimStatus;
import com.insurance.platform.entity.Customer;
import com.insurance.platform.entity.Document;
import com.insurance.platform.entity.Policy;
import com.insurance.platform.entity.PolicyStatus;
import com.insurance.platform.entity.Role;
import com.insurance.platform.entity.User;
import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.repository.ClaimRepository;
import com.insurance.platform.repository.CustomerRepository;
import com.insurance.platform.repository.DocumentRepository;
import com.insurance.platform.repository.PolicyRepository;
import com.insurance.platform.repository.UserRepository;
import com.insurance.platform.service.AuditLogService;
import com.insurance.platform.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ClaimResponse submit(ClaimRequest request, String actorEmail) {
        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + request.getPolicyId()));

        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new BadRequestException("Claims can only be submitted against active policies");
        }

        Claim claim = Claim.builder()
                .policy(policy)
                .claimAmount(request.getClaimAmount())
                .reason(request.getReason())
                .status(ClaimStatus.PENDING)
                .build();

        Claim saved = claimRepository.save(claim);

        int attachedCount = linkDocuments(saved, policy.getCustomer(), request.getDocumentIds());

        auditLogService.log(actorEmail != null ? actorEmail : policy.getCustomer().getEmail(), "SUBMIT", "CLAIM", saved.getId(),
                "Claim submitted for policy " + policy.getPolicyNumber() + " amount " + saved.getClaimAmount()
                        + (attachedCount > 0 ? " with " + attachedCount + " supporting document(s)" : ""));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ClaimResponse attachDocuments(Long claimId, List<Long> documentIds, String actorEmail) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        int attachedCount = linkDocuments(claim, claim.getPolicy().getCustomer(), documentIds);
        if (attachedCount == 0) {
            throw new BadRequestException("No valid documents were attached — make sure they belong to you and aren't already linked elsewhere");
        }

        auditLogService.log(actorEmail, "ATTACH_DOCUMENT", "CLAIM", claim.getId(),
                "Attached " + attachedCount + " supporting document(s) to claim");
        return toResponse(claim);
    }

    /** Links the given document IDs to a claim, but only documents owned by the claim's customer. */
    private int linkDocuments(Claim claim, Customer owner, List<Long> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return 0;
        }
        List<Document> documents = documentRepository.findAllById(documentIds);
        int count = 0;
        for (Document document : documents) {
            if (document.getCustomer().getId().equals(owner.getId())) {
                document.setClaim(claim);
                documentRepository.save(document);
                count++;
            }
        }
        return count;
    }

    @Override
    @Transactional
    public ClaimResponse assign(Long claimId, Long agentId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + agentId));
        if (agent.getRole() != Role.AGENT && agent.getRole() != Role.ADMIN) {
            throw new BadRequestException("Claims can only be assigned to Agent or Admin users");
        }
        claim.setAssignedAgent(agent);
        if (claim.getStatus() == ClaimStatus.PENDING) {
            claim.setStatus(ClaimStatus.UNDER_REVIEW);
        }
        Claim saved = claimRepository.save(claim);
        auditLogService.log(agent.getEmail(), "ASSIGN", "CLAIM", saved.getId(), "Claim assigned to " + agent.getName());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ClaimResponse decide(Long claimId, ClaimDecisionRequest request, String agentEmail, String requestingAgentEmail) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));
        assertOwnership(claim, requestingAgentEmail);

        if (request.getStatus() != ClaimStatus.APPROVED && request.getStatus() != ClaimStatus.REJECTED) {
            throw new BadRequestException("Decision status must be APPROVED or REJECTED");
        }

        claim.setStatus(request.getStatus());
        claim.setVerifiedByAgentEmail(agentEmail);
        claim.setRejectionRemarks(request.getRemarks());
        claim.setResolvedDate(LocalDateTime.now());

        Claim saved = claimRepository.save(claim);
        auditLogService.log(agentEmail, request.getStatus().name(), "CLAIM", saved.getId(),
                "Claim " + request.getStatus().name().toLowerCase() + (request.getRemarks() != null ? ": " + request.getRemarks() : ""));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ClaimResponse markUnderReview(Long claimId, String requestingAgentEmail) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));
        assertOwnership(claim, requestingAgentEmail);
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        return toResponse(claimRepository.save(claim));
    }

    @Override
    public ClaimResponse getById(Long id, String requestingAgentEmail) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + id));
        assertOwnership(claim, requestingAgentEmail);
        return toResponse(claim);
    }

    @Override
    public List<ClaimResponse> getAll(String requestingAgentEmail) {
        return claimRepository.findScoped(requestingAgentEmail).stream().map(this::toResponse).toList();
    }

    @Override
    public List<ClaimResponse> getByPolicy(Long policyId, String requestingAgentEmail) {
        List<Claim> claims = claimRepository.findByPolicyId(policyId);
        if (!claims.isEmpty()) {
            assertOwnership(claims.get(0), requestingAgentEmail);
        }
        return claims.stream().map(this::toResponse).toList();
    }

    @Override
    public List<ClaimResponse> getByCurrentCustomerUser(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for current user"));
        return claimRepository.findByPolicy_Customer_Id(customer.getId()).stream().map(this::toResponse).toList();
    }

    @Override
    public List<ClaimResponse> getPending() {
        return claimRepository.findByStatus(ClaimStatus.PENDING).stream().map(this::toResponse).toList();
    }

    @Override
    public List<ClaimResponse> getAssignedToAgent(String agentEmail) {
        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return claimRepository.findByAssignedAgent_Id(agent.getId()).stream().map(this::toResponse).toList();
    }

    @Override
    public PageResponse<ClaimResponse> filterPaged(ClaimStatus status, String search, String requestingAgentEmail, Pageable pageable) {
        Page<Claim> page = claimRepository.filter(requestingAgentEmail, status, (search == null || search.isBlank()) ? null : search, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    /** Throws if an Agent (requestingAgentEmail != null) tries to access a claim for a customer they didn't
     *  register AND that hasn't been explicitly assigned to them by an Admin. Admins (null) bypass this. */
    private void assertOwnership(Claim claim, String requestingAgentEmail) {
        if (requestingAgentEmail == null) {
            return; // Admin — unrestricted
        }
        boolean ownsCustomer = requestingAgentEmail.equalsIgnoreCase(claim.getPolicy().getCustomer().getRegisteredByAgentEmail());
        boolean assignedToMe = claim.getAssignedAgent() != null && requestingAgentEmail.equalsIgnoreCase(claim.getAssignedAgent().getEmail());
        if (!ownsCustomer && !assignedToMe) {
            throw new AccessDeniedException("You can only access claims for customers you registered, or claims assigned to you");
        }
    }

    private ClaimResponse toResponse(Claim claim) {
        List<ClaimDocumentResponse> documents = documentRepository.findByClaimId(claim.getId()).stream()
                .map(doc -> ClaimDocumentResponse.builder()
                        .id(doc.getId())
                        .fileName(doc.getFileName())
                        .documentType(doc.getDocumentType())
                        .uploadedAt(doc.getUploadedAt())
                        .downloadUrl("/api/documents/" + doc.getId() + "/download")
                        .build())
                .toList();

        return ClaimResponse.builder()
                .id(claim.getId())
                .policyId(claim.getPolicy().getId())
                .policyNumber(claim.getPolicy().getPolicyNumber())
                .customerId(claim.getPolicy().getCustomer().getId())
                .customerName(claim.getPolicy().getCustomer().getName())
                .claimAmount(claim.getClaimAmount())
                .reason(claim.getReason())
                .status(claim.getStatus())
                .assignedAgentId(claim.getAssignedAgent() != null ? claim.getAssignedAgent().getId() : null)
                .assignedAgentName(claim.getAssignedAgent() != null ? claim.getAssignedAgent().getName() : null)
                .rejectionRemarks(claim.getRejectionRemarks())
                .submissionDate(claim.getSubmissionDate())
                .resolvedDate(claim.getResolvedDate())
                .documents(documents)
                .build();
    }
}
