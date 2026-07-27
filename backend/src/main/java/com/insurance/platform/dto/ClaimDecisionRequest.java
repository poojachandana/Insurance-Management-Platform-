package com.insurance.platform.dto;

import com.insurance.platform.entity.ClaimStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimDecisionRequest {

    @NotNull
    private ClaimStatus status; // APPROVED or REJECTED

    private String remarks;
}
