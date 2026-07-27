package com.insurance.platform.dto;

import com.insurance.platform.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Optional - defaults to CUSTOMER if not supplied (admins can be seeded/created separately)
    private Role role;

    // Customer-specific optional fields (used when role = CUSTOMER)
    private String dob;
    private String phone;
    private String address;
}
