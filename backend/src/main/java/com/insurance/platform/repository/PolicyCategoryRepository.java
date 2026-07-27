package com.insurance.platform.repository;

import com.insurance.platform.entity.PolicyCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolicyCategoryRepository extends JpaRepository<PolicyCategory, Long> {
    List<PolicyCategory> findByActiveTrue();
    Optional<PolicyCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
