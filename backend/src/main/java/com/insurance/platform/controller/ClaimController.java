package com.insurance.platform.controller;

import com.insurance.platform.dto.AttachDocumentsRequest;
import com.insurance.platform.dto.ClaimAssignRequest;
import com.insurance.platform.dto.ClaimDecisionRequest;
import com.insurance.platform.dto.ClaimRequest;
import com.insurance.platform.dto.ClaimResponse;
import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.entity.ClaimStatus;
import com.insurance.platform.service.ClaimService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Tag(name = "Claim Management", description = "Submit, verify, approve/reject insurance claims")
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','AGENT')")
    public ResponseEntity<ClaimResponse> submit(@Valid @RequestBody ClaimRequest request, Authentication authentication) {
        return ResponseEntity.ok(claimService.submit(request, authentication.getName()));
    }

    @PatchMapping("/{id}/documents")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','AGENT')")
    public ResponseEntity<ClaimResponse> attachDocuments(@PathVariable Long id, @Valid @RequestBody AttachDocumentsRequest request,
                                                          Authentication authentication) {
        return ResponseEntity.ok(claimService.attachDocuments(id, request.getDocumentIds(), authentication.getName()));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponse> assign(@PathVariable Long id, @Valid @RequestBody ClaimAssignRequest request) {
        return ResponseEntity.ok(claimService.assign(id, request.getAgentId()));
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<ClaimResponse> markUnderReview(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(claimService.markUnderReview(id, agentScope(authentication)));
    }

    @PatchMapping("/{id}/decision")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<ClaimResponse> decide(@PathVariable Long id, @Valid @RequestBody ClaimDecisionRequest request,
                                                 Authentication authentication) {
        return ResponseEntity.ok(claimService.decide(id, request, authentication.getName(), agentScope(authentication)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> getById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(claimService.getById(id, agentScope(authentication)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<List<ClaimResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(claimService.getAll(agentScope(authentication)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<List<ClaimResponse>> getPending() {
        return ResponseEntity.ok(claimService.getPending());
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<PageResponse<ClaimResponse>> getAllPaged(
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "submissionDate", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(claimService.filterPaged(status, search, agentScope(authentication), pageable));
    }

    @GetMapping("/assigned/me")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<List<ClaimResponse>> getAssignedToMe(Authentication authentication) {
        return ResponseEntity.ok(claimService.getAssignedToAgent(authentication.getName()));
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<ClaimResponse>> getByPolicy(@PathVariable Long policyId, Authentication authentication) {
        return ResponseEntity.ok(claimService.getByPolicy(policyId, agentScope(authentication)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ClaimResponse>> getMyClaims(Authentication authentication) {
        return ResponseEntity.ok(claimService.getByCurrentCustomerUser(authentication.getName()));
    }

    /** Returns the caller's own email only when they hold the AGENT role (scoped view);
     *  null for Admins and Customers (unrestricted by this mechanism). */
    private String agentScope(Authentication authentication) {
        boolean isAgent = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"));
        return isAgent ? authentication.getName() : null;
    }
}
