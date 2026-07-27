package com.insurance.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Application-wide configuration, managed by an Administrator.
 * Stored as a single row (id = 1L) — see SystemSettingsServiceImpl.
 */
@Entity
@Table(name = "system_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSettings {

    @Id
    private Long id;

    @Column(nullable = false)
    private String companyName;

    private String supportEmail;

    private String supportPhone;

    /** Default policy term (in months) suggested when creating new policies. */
    @Column(nullable = false)
    private int defaultPolicyTermMonths;

    /** How many days before a policy's end date to raise an expiry-reminder notification. */
    @Column(nullable = false)
    private int policyExpiryReminderDays;

    /** Grace period (in days) after a premium's due date before it is marked OVERDUE. */
    @Column(nullable = false)
    private int premiumGraceDays;
}
