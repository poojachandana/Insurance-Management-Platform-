package com.insurance.platform.controller;

import com.insurance.platform.dto.EmployeeRequest;
import com.insurance.platform.dto.EmployeeResponse;
import com.insurance.platform.service.EmployeeService;
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
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Employee Management", description = "Administrator-only management of Agent and Admin accounts")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request, Authentication authentication) {
        return ResponseEntity.ok(employeeService.create(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request, Authentication authentication) {
        return ResponseEntity.ok(employeeService.update(id, request, authentication.getName()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EmployeeResponse> setEnabled(@PathVariable Long id, @RequestBody Map<String, Boolean> body, Authentication authentication) {
        boolean enabled = Boolean.TRUE.equals(body.get("enabled"));
        return ResponseEntity.ok(employeeService.setEnabled(id, enabled, authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAll() {
        return ResponseEntity.ok(employeeService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        employeeService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
