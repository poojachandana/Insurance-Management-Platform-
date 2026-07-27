package com.insurance.platform.dto;

import com.insurance.platform.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    // Required when creating a new employee; optional on update (leave blank to keep existing password)
    private String password;

    @NotNull
    private Role role; // ADMIN or AGENT
}
