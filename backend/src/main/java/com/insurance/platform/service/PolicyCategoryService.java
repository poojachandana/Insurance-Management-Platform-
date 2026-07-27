package com.insurance.platform.service;

import com.insurance.platform.dto.PolicyCategoryRequest;
import com.insurance.platform.dto.PolicyCategoryResponse;

import java.util.List;

public interface PolicyCategoryService {
    PolicyCategoryResponse create(PolicyCategoryRequest request, String actorEmail);
    PolicyCategoryResponse update(Long id, PolicyCategoryRequest request, String actorEmail);
    PolicyCategoryResponse setActive(Long id, boolean active, String actorEmail);
    List<PolicyCategoryResponse> getAll();
    List<PolicyCategoryResponse> getActive();
}
