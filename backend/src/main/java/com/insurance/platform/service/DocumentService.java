package com.insurance.platform.service;

import com.insurance.platform.dto.DocumentResponse;
import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.entity.DocumentType;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    DocumentResponse upload(Long customerId, Long claimId, DocumentType type, MultipartFile file, String actorEmail);
    List<DocumentResponse> getByCustomer(Long customerId, String requestingAgentEmail);
    List<DocumentResponse> getByClaim(Long claimId);
    List<DocumentResponse> getAll(String requestingAgentEmail);

    /** requestingAgentEmail: null for Admins (unrestricted), or the caller's email to scope to customers they registered. */
    PageResponse<DocumentResponse> filterPaged(DocumentType type, String search, String requestingAgentEmail, Pageable pageable);
    DocumentResponse getById(Long id, String requestingAgentEmail);
    String getRawFilePath(Long id);
    void delete(Long id, String actorEmail, String requestingAgentEmail);
}
