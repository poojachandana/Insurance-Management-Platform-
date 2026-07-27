package com.insurance.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingsResponse {
    private String companyName;
    private String supportEmail;
    private String supportPhone;
    private int defaultPolicyTermMonths;
    private int policyExpiryReminderDays;
    private int premiumGraceDays;
}
