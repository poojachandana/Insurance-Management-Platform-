package com.insurance.platform.service.impl;

import com.insurance.platform.dto.EmployeeRequest;
import com.insurance.platform.dto.EmployeeResponse;
import com.insurance.platform.entity.Role;
import com.insurance.platform.entity.User;
import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.DuplicateResourceException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.repository.UserRepository;
import com.insurance.platform.service.AuditLogService;
import com.insurance.platform.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request, String actorEmail) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with this email already exists");
        }
        validateEmployeeRole(request.getRole());
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required when creating an employee");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        auditLogService.log(actorEmail, "CREATE", "EMPLOYEE", saved.getId(), "Created " + saved.getRole() + " account for " + saved.getName());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request, String actorEmail) {
        User user = getEmployeeOrThrow(id);
        validateEmployeeRole(request.getRole());

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with this email already exists");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);
        auditLogService.log(actorEmail, "UPDATE", "EMPLOYEE", saved.getId(), "Updated employee " + saved.getName());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse setEnabled(Long id, boolean enabled, String actorEmail) {
        User user = getEmployeeOrThrow(id);
        if (!enabled) {
            guardLastAdmin(user);
        }
        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        auditLogService.log(actorEmail, enabled ? "ENABLE" : "DISABLE", "EMPLOYEE", saved.getId(), (enabled ? "Enabled" : "Disabled") + " account for " + saved.getName());
        return toResponse(saved);
    }

    @Override
    public EmployeeResponse getById(Long id) {
        return toResponse(getEmployeeOrThrow(id));
    }

    @Override
    public List<EmployeeResponse> getAll() {
        return userRepository.findByRoleIn(List.of(Role.ADMIN, Role.AGENT))
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void delete(Long id, String currentAdminEmail) {
        User user = getEmployeeOrThrow(id);
        if (user.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new BadRequestException("You cannot delete your own account");
        }
        guardLastAdmin(user);
        userRepository.delete(user);
        auditLogService.log(currentAdminEmail, "DELETE", "EMPLOYEE", id, "Removed employee " + user.getName());
    }

    private void guardLastAdmin(User user) {
        if (user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new BadRequestException("Cannot remove the last remaining administrator account");
        }
    }

    private void validateEmployeeRole(Role role) {
        if (role != Role.ADMIN && role != Role.AGENT) {
            throw new BadRequestException("Employee role must be ADMIN or AGENT");
        }
    }

    private User getEmployeeOrThrow(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        if (user.getRole() == Role.CUSTOMER) {
            throw new ResourceNotFoundException("Employee not found with id: " + id);
        }
        return user;
    }

    private EmployeeResponse toResponse(User user) {
        return EmployeeResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
