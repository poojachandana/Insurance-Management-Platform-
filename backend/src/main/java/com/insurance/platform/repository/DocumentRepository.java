package com.insurance.platform.repository;

import com.insurance.platform.entity.Document;
import com.insurance.platform.entity.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByCustomerId(Long customerId);
    List<Document> findByClaimId(Long claimId);

    @Query("select d from Document d where " +
           "(:agentEmail is null or d.customer.registeredByAgentEmail = :agentEmail) and " +
           "(:type is null or d.documentType = :type) and " +
           "(:search is null or lower(d.fileName) like lower(concat('%', :search, '%')) " +
           "or lower(d.customer.name) like lower(concat('%', :search, '%')))")
    Page<Document> filter(@Param("agentEmail") String agentEmail, @Param("type") DocumentType type,
                           @Param("search") String search, Pageable pageable);
}
