package com.insurance.platform.dto;

import com.insurance.platform.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    /** Populated only when the email changed, so the frontend can refresh its stored JWT (subject = email). */
    private String newToken;
}
