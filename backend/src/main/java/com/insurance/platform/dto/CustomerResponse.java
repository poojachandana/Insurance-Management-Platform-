package com.insurance.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private String name;
    private LocalDate dob;
    private String phone;
    private String address;
    private String email;
    private Long assignedAgentId;
    private String assignedAgentName;
    private LocalDateTime createdAt;
}