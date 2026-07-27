package com.insurance.platform.service.impl;

import com.insurance.platform.dto.AuditLogResponse;
import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.entity.AuditLog;
import com.insurance.platform.repository.AuditLogRepository;
import com.insurance.platform.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String actorEmail, String action, String entityType, Long entityId, String details) {
        try {
            AuditLog entry = AuditLog.builder()
                    .actorEmail(actorEmail != null ? actorEmail : "SYSTEM")
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            // Audit logging must never break the primary business operation.
            log.warn("Failed to write audit log entry: {}", e.getMessage());
        }
    }

    @Override
    public PageResponse<AuditLogResponse> search(String entityType, String actorEmail, Pageable pageable) {
        var page = auditLogRepository.filter(
                (entityType == null || entityType.isBlank()) ? null : entityType,
                (actorEmail == null || actorEmail.isBlank()) ? null : actorEmail,
                pageable);
        return PageResponse.from(page, this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .actorEmail(auditLog.getActorEmail())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .details(auditLog.getDetails())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}
