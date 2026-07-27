package com.insurance.platform.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AttachDocumentsRequest {

    @NotEmpty(message = "At least one document ID must be provided")
    private List<Long> documentIds;
}
