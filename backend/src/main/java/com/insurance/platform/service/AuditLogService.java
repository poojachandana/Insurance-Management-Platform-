package com.insurance.platform.service;

import com.insurance.platform.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    /** Fire-and-forget write, called from within business service methods. Never throws. */
    void log(String actorEmail, String action, String entityType, Long entityId, String details);
    PageResponse<com.insurance.platform.dto.AuditLogResponse> search(String entityType, String actorEmail, Pageable pageable);
}
