package com.insurance.platform.controller;

import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.dto.PremiumPaymentRequest;
import com.insurance.platform.dto.PremiumPaymentResponse;
import com.insurance.platform.entity.PaymentStatus;
import com.insurance.platform.service.PremiumPaymentService;
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
@RequestMapping("/api/premiums")
@RequiredArgsConstructor
@Tag(name = "Premium Tracking", description = "Record and track premium payments, due dates, overdue alerts")
public class PremiumPaymentController {

    private final PremiumPaymentService premiumPaymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<PremiumPaymentResponse> createDue(@Valid @RequestBody PremiumPaymentRequest request, Authentication authentication) {
        return ResponseEntity.ok(premiumPaymentService.createDue(request, authentication.getName(), agentScope(authentication)));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<PremiumPaymentResponse> pay(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(premiumPaymentService.recordPayment(id, authentication.getName(), agentScope(authentication)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<List<PremiumPaymentResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(premiumPaymentService.getAll(agentScope(authentication)));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<PageResponse<PremiumPaymentResponse>> getAllPaged(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(premiumPaymentService.filterPaged(status, search, agentScope(authentication), pageable));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<List<PremiumPaymentResponse>> getOverdue() {
        return ResponseEntity.ok(premiumPaymentService.getOverdue());
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<PremiumPaymentResponse>> getByPolicy(@PathVariable Long policyId, Authentication authentication) {
        return ResponseEntity.ok(premiumPaymentService.getByPolicy(policyId, agentScope(authentication)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<PremiumPaymentResponse>> getMyPayments(Authentication authentication) {
        return ResponseEntity.ok(premiumPaymentService.getByCurrentCustomerUser(authentication.getName()));
    }

    /** Returns the caller's own email only when they hold the AGENT role (scoped view);
     *  null for Admins and Customers (unrestricted by this mechanism). */
    private String agentScope(Authentication authentication) {
        boolean isAgent = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"));
        return isAgent ? authentication.getName() : null;
    }
}
