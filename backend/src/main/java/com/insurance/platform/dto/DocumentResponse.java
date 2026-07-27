package com.insurance.platform.dto;

import com.insurance.platform.entity.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long claimId;
    private String claimPolicyNumber;
    private String fileName;
    private DocumentType documentType;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
}
