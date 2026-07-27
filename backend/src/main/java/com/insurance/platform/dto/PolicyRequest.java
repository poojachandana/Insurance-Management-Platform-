package com.insurance.platform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PolicyRequest {

    @NotNull
    private Long customerId;

    @NotNull(message = "A policy category must be selected")
    private Long categoryId;

    @Positive
    private BigDecimal premiumAmount;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
