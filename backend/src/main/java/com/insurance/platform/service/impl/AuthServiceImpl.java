package com.insurance.platform.service.impl;

import com.insurance.platform.dto.AuthResponse;
import com.insurance.platform.dto.LoginRequest;
import com.insurance.platform.dto.RegisterRequest;
import com.insurance.platform.entity.Customer;
import com.insurance.platform.entity.Role;
import com.insurance.platform.entity.User;
import com.insurance.platform.exception.DuplicateResourceException;
import com.insurance.platform.repository.CustomerRepository;
import com.insurance.platform.repository.UserRepository;
import com.insurance.platform.security.CustomUserDetails;
import com.insurance.platform.security.JwtUtil;
import com.insurance.platform.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with this email already exists");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.CUSTOMER;
        // Prevent public self-registration as ADMIN
        if (role == Role.ADMIN) {
            role = Role.CUSTOMER;
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        if (role == Role.CUSTOMER) {
            linkOrCreateCustomerProfile(user, request);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());
        String token = jwtUtil.generateToken(new CustomUserDetails(user), claims);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    /**
     * An Agent may have already created a Customer profile (name/phone/address) for this
     * person before they ever self-registered — that profile has no linked User/login yet.
     * If we find one by email, link this new login to it instead of creating a duplicate
     * customer record (which previously caused two separate rows for the same person).
     */
    private void linkOrCreateCustomerProfile(User user, RegisterRequest request) {
        Optional<Customer> existing = customerRepository.findByEmail(request.getEmail());

        if (existing.isPresent()) {
            Customer customer = existing.get();
            if (customer.getUser() != null) {
                // Defensive guard — shouldn't happen since userRepository.existsByEmail already
                // caught this above, but avoids ever silently overwriting someone else's account.
                throw new DuplicateResourceException("A customer profile with this email already exists");
            }
            customer.setUser(user);
            // Fill in anything the Agent hadn't captured yet, without overwriting what they already entered.
            if (isBlank(customer.getPhone())) {
                customer.setPhone(request.getPhone());
            }
            if (isBlank(customer.getAddress())) {
                customer.setAddress(request.getAddress());
            }
            if (customer.getDob() == null && !isBlank(request.getDob())) {
                customer.setDob(LocalDate.parse(request.getDob()));
            }
            customerRepository.save(customer);
            return;
        }

        Customer customer = Customer.builder()
                .user(user)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .dob(!isBlank(request.getDob()) ? LocalDate.parse(request.getDob()) : null)
                .build();
        customerRepository.save(customer);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());
        String token = jwtUtil.generateToken(new CustomUserDetails(user), claims);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}