package com.insurance.platform.service;

import com.insurance.platform.dto.AccountResponse;
import com.insurance.platform.dto.ChangePasswordRequest;
import com.insurance.platform.dto.UpdateProfileRequest;

public interface AccountService {
    AccountResponse getMe(String email);
    AccountResponse updateProfile(String email, UpdateProfileRequest request);
    void changePassword(String email, ChangePasswordRequest request);
}
