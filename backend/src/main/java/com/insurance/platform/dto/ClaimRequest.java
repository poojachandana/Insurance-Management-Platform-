package com.insurance.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ClaimRequest {

    @NotNull
    private Long policyId;

    @Positive
    private BigDecimal claimAmount;

    @NotBlank
    private String reason;

    /** IDs of documents (already uploaded by the customer) to attach as supporting evidence. */
    private List<Long> documentIds;
}
