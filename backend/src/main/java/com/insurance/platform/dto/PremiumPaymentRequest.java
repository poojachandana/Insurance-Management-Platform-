package com.insurance.platform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PremiumPaymentRequest {

    @NotNull
    private Long policyId;

    @NotNull
    private LocalDate dueDate;

    @Positive
    private java.math.BigDecimal amount;
}
