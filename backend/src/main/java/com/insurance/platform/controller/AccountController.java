package com.insurance.platform.controller;

import com.insurance.platform.dto.AccountResponse;
import com.insurance.platform.dto.ChangePasswordRequest;
import com.insurance.platform.dto.UpdateProfileRequest;
import com.insurance.platform.service.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Tag(name = "My Account", description = "Self-service profile view/edit and password change for any logged-in user")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getMe(Authentication authentication) {
        return ResponseEntity.ok(accountService.getMe(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<AccountResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request, Authentication authentication) {
        return ResponseEntity.ok(accountService.updateProfile(authentication.getName(), request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        accountService.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
