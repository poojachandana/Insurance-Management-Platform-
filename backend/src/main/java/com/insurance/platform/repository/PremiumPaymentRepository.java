package com.insurance.platform.repository;

import com.insurance.platform.entity.PaymentStatus;
import com.insurance.platform.entity.PremiumPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PremiumPaymentRepository extends JpaRepository<PremiumPayment, Long> {
    List<PremiumPayment> findByPolicyId(Long policyId);
    List<PremiumPayment> findByPolicy_Customer_Id(Long customerId);
    List<PremiumPayment> findByPaymentStatus(PaymentStatus status);
    List<PremiumPayment> findByDueDateBeforeAndPaymentStatusNot(LocalDate date, PaymentStatus status);

    @Query("select p from PremiumPayment p where " +
            "(:agentEmail is null or p.policy.customer.registeredByAgentEmail = :agentEmail) and " +
            "(:status is null or p.paymentStatus = :status) and " +
            "(:search is null or lower(p.policy.policyNumber) like lower(concat('%', cast(:search as string), '%')) " +
            "or lower(p.policy.customer.name) like lower(concat('%', cast(:search as string), '%')))")
    Page<PremiumPayment> filter(@Param("agentEmail") String agentEmail, @Param("status") PaymentStatus status,
                                @Param("search") String search, Pageable pageable);

    @Query("select p from PremiumPayment p where (:agentEmail is null or p.policy.customer.registeredByAgentEmail = :agentEmail)")
    List<PremiumPayment> findScoped(@Param("agentEmail") String agentEmail);
}