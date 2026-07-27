package com.insurance.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A structured insurance category (e.g. Health, Life, Motor, Home, Travel),
 * managed by Administrators, and offered as a dropdown when Agents create a
 * new Policy — replacing free-text policy types (Multiple Insurance Categories).
 */
@Entity
@Table(name = "policy_categories", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean active;
}
