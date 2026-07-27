package com.insurance.platform.service.impl;

import com.insurance.platform.dto.DocumentResponse;
import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.entity.Claim;
import com.insurance.platform.entity.Customer;
import com.insurance.platform.entity.Document;
import com.insurance.platform.entity.DocumentType;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.repository.ClaimRepository;
import com.insurance.platform.repository.CustomerRepository;
import com.insurance.platform.repository.DocumentRepository;
import com.insurance.platform.service.AuditLogService;
import com.insurance.platform.service.DocumentService;
import com.insurance.platform.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final CustomerRepository customerRepository;
    private final ClaimRepository claimRepository;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public DocumentResponse upload(Long customerId, Long claimId, DocumentType type, MultipartFile file, String actorEmail) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        Claim claim = null;
        if (claimId != null) {
            claim = claimRepository.findById(claimId)
                    .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));
        }

        String subDir = "customer_" + customerId;
        String storedPath = fileStorageService.store(file, subDir);

        Document document = Document.builder()
                .customer(customer)
                .claim(claim)
                .fileName(file.getOriginalFilename())
                .filePath(storedPath)
                .documentType(type)
                .build();

        Document saved = documentRepository.save(document);
        auditLogService.log(actorEmail, "UPLOAD", "DOCUMENT", saved.getId(),
                "Uploaded " + type + " document \"" + saved.getFileName() + "\" for " + customer.getName());
        return toResponse(saved);
    }

    @Override
    public List<DocumentResponse> getByCustomer(Long customerId, String requestingAgentEmail) {
        if (requestingAgentEmail != null) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
            if (!requestingAgentEmail.equalsIgnoreCase(customer.getRegisteredByAgentEmail())) {
                throw new AccessDeniedException("You can only view documents for customers you registered");
            }
        }
        return documentRepository.findByCustomerId(customerId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<DocumentResponse> getByClaim(Long claimId) {
        return documentRepository.findByClaimId(claimId).stream().map(this::toResponse).toList();
    }

    @Override
    public List<DocumentResponse> getAll(String requestingAgentEmail) {
        return documentRepository.findAll().stream()
                .filter(d -> requestingAgentEmail == null || requestingAgentEmail.equalsIgnoreCase(d.getCustomer().getRegisteredByAgentEmail()))
                .map(this::toResponse).toList();
    }

    @Override
    public PageResponse<DocumentResponse> filterPaged(DocumentType type, String search, String requestingAgentEmail, Pageable pageable) {
        Page<Document> page = documentRepository.filter(requestingAgentEmail, type,
                (search == null || search.isBlank()) ? null : search, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    public DocumentResponse getById(Long id, String requestingAgentEmail) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        assertOwnership(document, requestingAgentEmail);
        return toResponse(document);
    }

    @Override
    public String getRawFilePath(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        return document.getFilePath();
    }

    @Override
    @Transactional
    public void delete(Long id, String actorEmail, String requestingAgentEmail) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        assertOwnership(document, requestingAgentEmail);
        documentRepository.deleteById(id);
        auditLogService.log(actorEmail, "DELETE", "DOCUMENT", id, "Deleted document \"" + document.getFileName() + "\"");
    }

    /** Throws if an Agent (requestingAgentEmail != null) tries to access a document for a customer they didn't register. Admins (null) bypass this. */
    private void assertOwnership(Document document, String requestingAgentEmail) {
        if (requestingAgentEmail == null) {
            return; // Admin — unrestricted
        }
        boolean owns = requestingAgentEmail.equalsIgnoreCase(document.getCustomer().getRegisteredByAgentEmail());
        if (!owns) {
            throw new AccessDeniedException("You can only access documents for customers you registered");
        }
    }

    private DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .customerId(document.getCustomer().getId())
                .customerName(document.getCustomer().getName())
                .claimId(document.getClaim() != null ? document.getClaim().getId() : null)
                .claimPolicyNumber(document.getClaim() != null ? document.getClaim().getPolicy().getPolicyNumber() : null)
                .fileName(document.getFileName())
                .documentType(document.getDocumentType())
                .uploadedAt(document.getUploadedAt())
                .downloadUrl("/api/documents/" + document.getId() + "/download")
                .build();
    }
}
