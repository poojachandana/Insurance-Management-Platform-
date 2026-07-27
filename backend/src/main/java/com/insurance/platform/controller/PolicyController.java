package com.insurance.platform.controller;

import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.dto.PolicyRequest;
import com.insurance.platform.dto.PolicyResponse;
import com.insurance.platform.entity.PolicyStatus;
import com.insurance.platform.service.PolicyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Policy Management", description = "Create, view, renew, cancel insurance policies")
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<PolicyResponse> create(@Valid @RequestBody PolicyRequest request, Authentication authentication) {
        return ResponseEntity.ok(policyService.create(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<PolicyResponse> update(@PathVariable Long id, @Valid @RequestBody PolicyRequest request, Authentication authentication) {
        return ResponseEntity.ok(policyService.update(id, request, authentication.getName(), agentScope(authentication)));
    }

    @PatchMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<PolicyResponse> renew(@PathVariable Long id, @RequestParam(defaultValue = "12") int extendMonths, Authentication authentication) {
        return ResponseEntity.ok(policyService.renew(id, extendMonths, authentication.getName(), agentScope(authentication)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<PolicyResponse> cancel(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(policyService.cancel(id, authentication.getName(), agentScope(authentication)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponse> getById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(policyService.getById(id, agentScope(authentication)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<List<PolicyResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(policyService.getAll(agentScope(authentication)));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<PageResponse<PolicyResponse>> getAllPaged(
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "id") Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(policyService.filterPaged(status, search, agentScope(authentication), pageable));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<List<PolicyResponse>> getByCustomer(@PathVariable Long customerId, Authentication authentication) {
        return ResponseEntity.ok(policyService.getByCustomer(customerId, agentScope(authentication)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<PolicyResponse>> getMyPolicies(Authentication authentication) {
        return ResponseEntity.ok(policyService.getByCurrentCustomerUser(authentication.getName()));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<List<PolicyResponse>> getExpiringSoon(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(policyService.getExpiringWithinDays(days));
    }

    /** Returns the caller's own email only when they hold the AGENT role (scoped view);
     *  null for Admins and Customers (unrestricted by this mechanism). */
    private String agentScope(Authentication authentication) {
        boolean isAgent = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"));
        return isAgent ? authentication.getName() : null;
    }
}
