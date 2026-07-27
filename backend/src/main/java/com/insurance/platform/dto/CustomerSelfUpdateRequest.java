package com.insurance.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerSelfUpdateRequest {

    @NotBlank
    private String name;

    private LocalDate dob;

    private String phone;

    private String address;
}
