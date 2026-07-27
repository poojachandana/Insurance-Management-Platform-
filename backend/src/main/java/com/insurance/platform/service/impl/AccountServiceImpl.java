package com.insurance.platform.service.impl;

import com.insurance.platform.dto.AccountResponse;
import com.insurance.platform.dto.ChangePasswordRequest;
import com.insurance.platform.dto.UpdateProfileRequest;
import com.insurance.platform.entity.Customer;
import com.insurance.platform.entity.User;
import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.DuplicateResourceException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.repository.CustomerRepository;
import com.insurance.platform.repository.UserRepository;
import com.insurance.platform.security.CustomUserDetails;
import com.insurance.platform.security.JwtUtil;
import com.insurance.platform.service.AccountService;
import com.insurance.platform.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final JwtUtil jwtUtil;

    @Override
    public AccountResponse getMe(String email) {
        User user = getUserOrThrow(email);
        return toResponse(user);
    }

    @Override
    @Transactional
    public AccountResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = getUserOrThrow(email);

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with this email already exists");
        }

        String oldEmail = user.getEmail();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        User saved = userRepository.save(user);

        // Keep the linked Customer record's contact details in sync so login email stays consistent.
        customerRepository.findByUser_Id(user.getId()).ifPresent(customer -> {
            customer.setName(request.getName());
            customer.setEmail(request.getEmail());
            customerRepository.save(customer);
        });

        auditLogService.log(oldEmail, "UPDATE", "ACCOUNT", saved.getId(),
                "Profile updated" + (!oldEmail.equalsIgnoreCase(saved.getEmail()) ? " (email changed to " + saved.getEmail() + ")" : ""));

        String newToken = null;
        if (!oldEmail.equalsIgnoreCase(saved.getEmail())) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", saved.getRole().name());
            claims.put("userId", saved.getId());
            newToken = jwtUtil.generateToken(new CustomUserDetails(saved), claims);
        }

        return toResponse(saved, newToken);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getUserOrThrow(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditLogService.log(email, "PASSWORD_CHANGE", "ACCOUNT", user.getId(), "Password changed by user");
    }

    private User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AccountResponse toResponse(User user) {
        return toResponse(user, null);
    }

    private AccountResponse toResponse(User user, String newToken) {
        return AccountResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .newToken(newToken)
                .build();
    }
}
