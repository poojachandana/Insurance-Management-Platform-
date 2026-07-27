package com.insurance.platform.repository;

import com.insurance.platform.entity.Policy;
import com.insurance.platform.entity.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByCustomerId(Long customerId);
    List<Policy> findByStatus(PolicyStatus status);
    long countByStatus(PolicyStatus status);
    List<Policy> findByEndDateBetweenAndStatus(LocalDate start, LocalDate end, PolicyStatus status);

    @Query("select p from Policy p where " +
           "(:agentEmail is null or p.customer.registeredByAgentEmail = :agentEmail) and " +
           "(:status is null or p.status = :status) and " +
           "(:search is null or lower(p.policyNumber) like lower(concat('%', :search, '%')) " +
           "or lower(p.customer.name) like lower(concat('%', :search, '%')) " +
           "or lower(p.policyType) like lower(concat('%', :search, '%')))")
    Page<Policy> filter(@Param("agentEmail") String agentEmail, @Param("status") PolicyStatus status,
                         @Param("search") String search, Pageable pageable);

    @Query("select p from Policy p where (:agentEmail is null or p.customer.registeredByAgentEmail = :agentEmail)")
    List<Policy> findScoped(@Param("agentEmail") String agentEmail);
}
