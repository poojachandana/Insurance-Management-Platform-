package com.insurance.platform.repository;

import com.insurance.platform.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByUser_Id(Long userId);

    @Query("select c from Customer c where " +
            "(:agentEmail is null or c.registeredByAgentEmail = :agentEmail) and " +
            "(:keyword is null or lower(c.name) like lower(concat('%', cast(:keyword as string), '%')) " +
            "or lower(c.email) like lower(concat('%', cast(:keyword as string), '%')) " +
            "or c.phone like concat('%', cast(:keyword as string), '%'))")
    List<Customer> findScoped(@Param("agentEmail") String agentEmail, @Param("keyword") String keyword);

    @Query("select c from Customer c where " +
            "(:agentEmail is null or c.registeredByAgentEmail = :agentEmail) and " +
            "(:keyword is null or lower(c.name) like lower(concat('%', cast(:keyword as string), '%')) " +
            "or lower(c.email) like lower(concat('%', cast(:keyword as string), '%')) " +
            "or c.phone like concat('%', cast(:keyword as string), '%'))")
    Page<Customer> findScoped(@Param("agentEmail") String agentEmail, @Param("keyword") String keyword, Pageable pageable);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}