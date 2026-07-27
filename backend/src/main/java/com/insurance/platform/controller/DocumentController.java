package com.insurance.platform.controller;

import com.insurance.platform.dto.DocumentResponse;
import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.entity.DocumentType;
import com.insurance.platform.service.DocumentService;
import com.insurance.platform.service.FileStorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "Upload, view and download identity/policy/claim documents")
public class DocumentController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<DocumentResponse> upload(@RequestParam Long customerId,
                                                    @RequestParam(required = false) Long claimId,
                                                    @RequestParam DocumentType documentType,
                                                    @RequestParam("file") MultipartFile file,
                                                    Authentication authentication) {
        return ResponseEntity.ok(documentService.upload(customerId, claimId, documentType, file, authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<List<DocumentResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(documentService.getAll(agentScope(authentication)));
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<PageResponse<DocumentResponse>> getAllPaged(
            @RequestParam(required = false) DocumentType type,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "uploadedAt") Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(documentService.filterPaged(type, search, agentScope(authentication), pageable));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<DocumentResponse>> getByCustomer(@PathVariable Long customerId, Authentication authentication) {
        return ResponseEntity.ok(documentService.getByCustomer(customerId, agentScope(authentication)));
    }

    @GetMapping("/claim/{claimId}")
    public ResponseEntity<List<DocumentResponse>> getByClaim(@PathVariable Long claimId) {
        return ResponseEntity.ok(documentService.getByClaim(claimId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id, Authentication authentication) {
        DocumentResponse doc = documentService.getById(id, agentScope(authentication));
        String rawFilePath = documentService.getRawFilePath(id);
        Resource resource = fileStorageService.loadAsResource(rawFilePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        documentService.delete(id, authentication.getName(), agentScope(authentication));
        return ResponseEntity.noContent().build();
    }

    /** Returns the caller's own email only when they hold the AGENT role (scoped view);
     *  null for Admins and Customers (unrestricted by this mechanism). */
    private String agentScope(Authentication authentication) {
        boolean isAgent = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AGENT"));
        return isAgent ? authentication.getName() : null;
    }
}
