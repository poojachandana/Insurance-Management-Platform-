package com.insurance.platform.dto;

import com.insurance.platform.entity.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Lightweight document summary embedded in ClaimResponse, so agents can see and
 *  verify supporting documents inline on the Claims page without an extra call. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDocumentResponse {
    private Long id;
    private String fileName;
    private DocumentType documentType;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
}
