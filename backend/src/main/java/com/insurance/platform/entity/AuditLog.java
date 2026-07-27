package com.insurance.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Immutable record of a significant action taken in the system, for
 * traceability/compliance (Audit Logs feature). Written by AuditLogService
 * from within the relevant business service methods — never updated or
 * deleted afterwards.
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Email of the user who performed the action, or "SYSTEM" for scheduled jobs. */
    @Column(nullable = false)
    private String actorEmail;

    /** Short verb describing what happened, e.g. CREATE, UPDATE, DELETE, LOGIN, APPROVE, REJECT. */
    @Column(nullable = false)
    private String action;

    /** The kind of entity affected, e.g. CUSTOMER, POLICY, CLAIM, PREMIUM, DOCUMENT, EMPLOYEE, SETTINGS. */
    @Column(nullable = false)
    private String entityType;

    private Long entityId;

    @Column(length = 1000)
    private String details;

    @Column(updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
