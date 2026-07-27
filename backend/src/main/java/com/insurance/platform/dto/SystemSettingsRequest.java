package com.insurance.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SystemSettingsRequest {

    @NotBlank
    private String companyName;

    private String supportEmail;

    private String supportPhone;

    @Min(1)
    private int defaultPolicyTermMonths;

    @Min(1)
    private int policyExpiryReminderDays;

    @Min(0)
    private int premiumGraceDays;
}
