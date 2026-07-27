package com.insurance.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    @JsonIgnore
    private User user;

    @NotBlank
    @Column(nullable = false)
    private String name;

    private LocalDate dob;

    private String phone;

    private String address;

    @Email
    @Column(nullable = false)
    private String email;

    /** Email of the Agent (or Admin) who registered this customer — used to scope
     *  what each Agent can see. Customers registered by an Admin, or via public
     *  self-registration, have this set to the Admin's email or null respectively. */
    private String registeredByAgentEmail;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
