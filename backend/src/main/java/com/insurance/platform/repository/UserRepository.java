package com.insurance.platform.repository;

import com.insurance.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    java.util.List<User> findByRoleIn(java.util.Collection<com.insurance.platform.entity.Role> roles);
    long countByRole(com.insurance.platform.entity.Role role);
}
