package com.insurance.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimAssignRequest {

    @NotNull
    private Long agentId;
}
