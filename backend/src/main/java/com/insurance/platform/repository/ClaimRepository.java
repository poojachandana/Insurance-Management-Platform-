package com.insurance.platform.repository;

import com.insurance.platform.entity.Claim;
import com.insurance.platform.entity.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByPolicyId(Long policyId);
    List<Claim> findByPolicy_Customer_Id(Long customerId);
    long countByStatus(ClaimStatus status);
    List<Claim> findByStatus(ClaimStatus status);
    List<Claim> findByAssignedAgent_Id(Long agentId);

    @Query("select c from Claim c left join c.assignedAgent a where " +
            "(:agentEmail is null or c.policy.customer.registeredByAgentEmail = :agentEmail " +
            "or a.email = :agentEmail) and " +
            "(:status is null or c.status = :status) and " +
            "(:search is null or lower(c.policy.policyNumber) like lower(concat('%', cast(:search as string), '%')) " +
            "or lower(c.policy.customer.name) like lower(concat('%', cast(:search as string), '%')) " +
            "or lower(c.reason) like lower(concat('%', cast(:search as string), '%')))")
    Page<Claim> filter(@Param("agentEmail") String agentEmail, @Param("status") ClaimStatus status,
                       @Param("search") String search, Pageable pageable);

    @Query("select c from Claim c left join c.assignedAgent a where " +
            "(:agentEmail is null or c.policy.customer.registeredByAgentEmail = :agentEmail " +
            "or a.email = :agentEmail)")
    List<Claim> findScoped(@Param("agentEmail") String agentEmail);
}