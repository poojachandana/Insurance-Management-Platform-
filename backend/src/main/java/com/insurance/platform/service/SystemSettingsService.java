package com.insurance.platform.service;

import com.insurance.platform.dto.SystemSettingsRequest;
import com.insurance.platform.dto.SystemSettingsResponse;
import com.insurance.platform.entity.SystemSettings;

public interface SystemSettingsService {
    SystemSettingsResponse getSettings();
    SystemSettingsResponse updateSettings(SystemSettingsRequest request, String actorEmail);
    /** Internal helper for other services (scheduled jobs) that need the raw entity. */
    SystemSettings getOrCreateDefault();
}
