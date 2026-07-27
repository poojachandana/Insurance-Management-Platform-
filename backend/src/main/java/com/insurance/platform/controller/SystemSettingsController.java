package com.insurance.platform.controller;

import com.insurance.platform.dto.SystemSettingsRequest;
import com.insurance.platform.dto.SystemSettingsResponse;
import com.insurance.platform.service.SystemSettingsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Tag(name = "System Settings", description = "Application-wide configuration managed by Administrators")
public class SystemSettingsController {

    private final SystemSettingsService systemSettingsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<SystemSettingsResponse> get() {
        return ResponseEntity.ok(systemSettingsService.getSettings());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemSettingsResponse> update(@Valid @RequestBody SystemSettingsRequest request, org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(systemSettingsService.updateSettings(request, authentication.getName()));
    }
}
