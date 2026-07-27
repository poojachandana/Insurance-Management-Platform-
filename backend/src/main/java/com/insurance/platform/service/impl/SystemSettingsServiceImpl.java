package com.insurance.platform.service.impl;

import com.insurance.platform.dto.SystemSettingsRequest;
import com.insurance.platform.dto.SystemSettingsResponse;
import com.insurance.platform.entity.SystemSettings;
import com.insurance.platform.repository.SystemSettingsRepository;
import com.insurance.platform.service.AuditLogService;
import com.insurance.platform.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemSettingsServiceImpl implements SystemSettingsService {

    private static final Long SINGLETON_ID = 1L;

    private final SystemSettingsRepository systemSettingsRepository;
    private final AuditLogService auditLogService;

    @Override
    public SystemSettingsResponse getSettings() {
        return toResponse(getOrCreateDefault());
    }

    @Override
    @Transactional
    public SystemSettingsResponse updateSettings(SystemSettingsRequest request, String actorEmail) {
        SystemSettings settings = getOrCreateDefault();
        settings.setCompanyName(request.getCompanyName());
        settings.setSupportEmail(request.getSupportEmail());
        settings.setSupportPhone(request.getSupportPhone());
        settings.setDefaultPolicyTermMonths(request.getDefaultPolicyTermMonths());
        settings.setPolicyExpiryReminderDays(request.getPolicyExpiryReminderDays());
        settings.setPremiumGraceDays(request.getPremiumGraceDays());
        SystemSettings saved = systemSettingsRepository.save(settings);
        auditLogService.log(actorEmail, "UPDATE", "SETTINGS", saved.getId(), "System settings updated");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SystemSettings getOrCreateDefault() {
        return systemSettingsRepository.findById(SINGLETON_ID).orElseGet(() -> {
            SystemSettings defaults = SystemSettings.builder()
                    .id(SINGLETON_ID)
                    .companyName("Insurance Management Platform")
                    .supportEmail("support@insurance.com")
                    .supportPhone("1800-000-0000")
                    .defaultPolicyTermMonths(12)
                    .policyExpiryReminderDays(30)
                    .premiumGraceDays(5)
                    .build();
            return systemSettingsRepository.save(defaults);
        });
    }

    private SystemSettingsResponse toResponse(SystemSettings settings) {
        return SystemSettingsResponse.builder()
                .companyName(settings.getCompanyName())
                .supportEmail(settings.getSupportEmail())
                .supportPhone(settings.getSupportPhone())
                .defaultPolicyTermMonths(settings.getDefaultPolicyTermMonths())
                .policyExpiryReminderDays(settings.getPolicyExpiryReminderDays())
                .premiumGraceDays(settings.getPremiumGraceDays())
                .build();
    }
}
