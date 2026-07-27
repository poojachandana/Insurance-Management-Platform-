package com.insurance.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PolicyCategoryRequest {

    @NotBlank
    private String name;

    private String description;
}
