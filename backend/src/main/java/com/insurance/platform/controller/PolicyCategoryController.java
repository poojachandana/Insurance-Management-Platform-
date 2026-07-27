package com.insurance.platform.controller;

import com.insurance.platform.dto.PolicyCategoryRequest;
import com.insurance.platform.dto.PolicyCategoryResponse;
import com.insurance.platform.service.PolicyCategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/policy-categories")
@RequiredArgsConstructor
@Tag(name = "Insurance Categories", description = "Structured policy categories (Health, Life, Motor, etc.), managed by Administrators")
public class PolicyCategoryController {

    private final PolicyCategoryService policyCategoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyCategoryResponse> create(@Valid @RequestBody PolicyCategoryRequest request, Authentication authentication) {
        return ResponseEntity.ok(policyCategoryService.create(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyCategoryResponse> update(@PathVariable Long id, @Valid @RequestBody PolicyCategoryRequest request, Authentication authentication) {
        return ResponseEntity.ok(policyCategoryService.update(id, request, authentication.getName()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyCategoryResponse> setActive(@PathVariable Long id, @RequestBody Map<String, Boolean> body, Authentication authentication) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        return ResponseEntity.ok(policyCategoryService.setActive(id, active, authentication.getName()));
    }

    /** All categories, including inactive ones — used by the Admin management screen. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<List<PolicyCategoryResponse>> getAll() {
        return ResponseEntity.ok(policyCategoryService.getAll());
    }

    /** Active categories only — used to populate the dropdown when creating a policy. */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<List<PolicyCategoryResponse>> getActive() {
        return ResponseEntity.ok(policyCategoryService.getActive());
    }
}
