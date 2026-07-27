package com.insurance.platform.dto;

import com.insurance.platform.entity.ClaimStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {
    private Long id;
    private Long policyId;
    private String policyNumber;
    private Long customerId;
    private String customerName;
    private BigDecimal claimAmount;
    private String reason;
    private ClaimStatus status;
    private Long assignedAgentId;
    private String assignedAgentName;
    private String rejectionRemarks;
    private LocalDateTime submissionDate;
    private LocalDateTime resolvedDate;
    private List<ClaimDocumentResponse> documents;
}
