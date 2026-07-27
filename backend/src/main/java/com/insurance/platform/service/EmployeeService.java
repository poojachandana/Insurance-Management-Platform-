package com.insurance.platform.service;

import com.insurance.platform.dto.EmployeeRequest;
import com.insurance.platform.dto.EmployeeResponse;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse create(EmployeeRequest request, String actorEmail);
    EmployeeResponse update(Long id, EmployeeRequest request, String actorEmail);
    EmployeeResponse setEnabled(Long id, boolean enabled, String actorEmail);
    EmployeeResponse getById(Long id);
    List<EmployeeResponse> getAll();
    void delete(Long id, String currentAdminEmail);
}
