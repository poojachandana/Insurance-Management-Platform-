package com.insurance.platform.service.impl;

import com.insurance.platform.dto.PolicyCategoryRequest;
import com.insurance.platform.dto.PolicyCategoryResponse;
import com.insurance.platform.entity.PolicyCategory;
import com.insurance.platform.exception.DuplicateResourceException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.repository.PolicyCategoryRepository;
import com.insurance.platform.service.AuditLogService;
import com.insurance.platform.service.PolicyCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyCategoryServiceImpl implements PolicyCategoryService {

    private final PolicyCategoryRepository policyCategoryRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public PolicyCategoryResponse create(PolicyCategoryRequest request, String actorEmail) {
        if (policyCategoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("A category with this name already exists");
        }
        PolicyCategory category = PolicyCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();
        category = policyCategoryRepository.save(category);
        auditLogService.log(actorEmail, "CREATE", "POLICY_CATEGORY", category.getId(), "Created category " + category.getName());
        return toResponse(category);
    }

    @Override
    @Transactional
    public PolicyCategoryResponse update(Long id, PolicyCategoryRequest request, String actorEmail) {
        PolicyCategory category = getOrThrow(id);
        if (!category.getName().equalsIgnoreCase(request.getName()) && policyCategoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("A category with this name already exists");
        }
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        PolicyCategory saved = policyCategoryRepository.save(category);
        auditLogService.log(actorEmail, "UPDATE", "POLICY_CATEGORY", saved.getId(), "Updated category " + saved.getName());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public PolicyCategoryResponse setActive(Long id, boolean active, String actorEmail) {
        PolicyCategory category = getOrThrow(id);
        category.setActive(active);
        PolicyCategory saved = policyCategoryRepository.save(category);
        auditLogService.log(actorEmail, active ? "ACTIVATE" : "DEACTIVATE", "POLICY_CATEGORY", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Override
    public List<PolicyCategoryResponse> getAll() {
        return policyCategoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<PolicyCategoryResponse> getActive() {
        return policyCategoryRepository.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    private PolicyCategory getOrThrow(Long id) {
        return policyCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy category not found with id: " + id));
    }

    private PolicyCategoryResponse toResponse(PolicyCategory category) {
        return PolicyCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.isActive())
                .build();
    }
}
