package com.insurance.platform.repository;

import com.insurance.platform.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("select a from AuditLog a where " +
           "(:entityType is null or a.entityType = :entityType) and " +
           "(:actorEmail is null or lower(a.actorEmail) like lower(concat('%', :actorEmail, '%')))")
    Page<AuditLog> filter(@Param("entityType") String entityType,
                           @Param("actorEmail") String actorEmail,
                           Pageable pageable);
}
