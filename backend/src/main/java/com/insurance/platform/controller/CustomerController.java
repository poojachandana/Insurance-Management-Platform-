package com.insurance.platform.controller;

import com.insurance.platform.dto.AssignAgentRequest;
import com.insurance.platform.dto.CustomerRequest;
import com.insurance.platform.dto.CustomerResponse;
import com.insurance.platform.dto.CustomerSelfUpdateRequest;
import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.service.CustomerService;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Register, view, edit, search customers")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request, Authentication authentication) {
        return ResponseEntity.ok(customerService.create(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request, Authentication authentication) {
        return ResponseEntity.ok(customerService.update(id, request, authentication.getName(), scopeEmail(authentication)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(customerService.getById(id, scopeEmail(authentication)));
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(customerService.getByCurrentUser(authentication.getName()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponse> updateMyProfile(@Valid @RequestBody CustomerSelfUpdateRequest request, Authentication authentication) {
        return ResponseEntity.ok(customerService.updateOwnProfile(authentication.getName(), request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<List<CustomerResponse>> getAll(@RequestParam(required = false) String search, Authentication authentication) {
        String scope = scopeEmail(authentication);
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(customerService.search(search, scope));
        }
        return ResponseEntity.ok(customerService.getAll(scope));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<PageResponse<CustomerResponse>> getAllPaged(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "name") Pageable pageable,
            Authentication authentication) {
        String scope = scopeEmail(authentication);
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(customerService.searchPaged(search, scope, pageable));
        }
        return ResponseEntity.ok(customerService.getAllPaged(scope, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        customerService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/assign-agent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> assignAgent(@PathVariable Long id, @RequestBody AssignAgentRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.ok(customerService.assignAgent(id, request.getAgentId(), authentication.getName()));
    }

    private String scopeEmail(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin ? null : authentication.getName();
    }
}