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

    /**
     * An Agent (agentEmail != null) sees a claim if either they registered the
     * underlying customer, OR an Admin has explicitly assigned this specific
     * claim to them (a deliberate cross-customer hand-off). Admins (agentEmail
     * = null) see everything.
     *
     * NOTE: uses an explicit LEFT JOIN on assignedAgent (a nullable association).
     * Navigating a nullable association via implicit dot-path (c.assignedAgent.email)
     * silently generates an INNER JOIN, which would drop every unassigned claim
     * from the result set entirely — before the WHERE/OR logic even runs.
     */
    @Query("select c from Claim c left join c.assignedAgent a where " +
            "(:agentEmail is null or c.policy.customer.registeredByAgentEmail = :agentEmail " +
            "or a.email = :agentEmail) and " +
            "(:status is null or c.status = :status) and " +
            "(:search is null or lower(c.policy.policyNumber) like lower(concat('%', :search, '%')) " +
            "or lower(c.policy.customer.name) like lower(concat('%', :search, '%')) " +
            "or lower(c.reason) like lower(concat('%', :search, '%')))")
    Page<Claim> filter(@Param("agentEmail") String agentEmail, @Param("status") ClaimStatus status,
                       @Param("search") String search, Pageable pageable);

    @Query("select c from Claim c left join c.assignedAgent a where " +
            "(:agentEmail is null or c.policy.customer.registeredByAgentEmail = :agentEmail " +
            "or a.email = :agentEmail)")
    List<Claim> findScoped(@Param("agentEmail") String agentEmail);
}