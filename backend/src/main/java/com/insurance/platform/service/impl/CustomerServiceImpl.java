package com.insurance.platform.service.impl;

import com.insurance.platform.dto.CustomerRequest;
import com.insurance.platform.dto.CustomerResponse;
import com.insurance.platform.dto.CustomerSelfUpdateRequest;
import com.insurance.platform.dto.PageResponse;
import com.insurance.platform.entity.Customer;
import com.insurance.platform.entity.Role;
import com.insurance.platform.entity.User;
import com.insurance.platform.exception.BadRequestException;
import com.insurance.platform.exception.DuplicateResourceException;
import com.insurance.platform.exception.ResourceNotFoundException;
import com.insurance.platform.repository.CustomerRepository;
import com.insurance.platform.repository.UserRepository;
import com.insurance.platform.service.AuditLogService;
import com.insurance.platform.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request, String actorEmail) {
        customerRepository.findByEmail(request.getEmail()).ifPresent(c -> {
            throw new DuplicateResourceException("A customer with this email already exists");
        });
        Customer customer = Customer.builder()
                .name(request.getName())
                .dob(request.getDob())
                .phone(request.getPhone())
                .address(request.getAddress())
                .email(request.getEmail())
                .registeredByAgentEmail(actorEmail)
                .build();
        customer = customerRepository.save(customer);
        auditLogService.log(actorEmail, "CREATE", "CUSTOMER", customer.getId(), "Registered customer " + customer.getName());
        return toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request, String actorEmail, String requestingAgentEmail) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        assertOwnership(customer, requestingAgentEmail);
        customer.setName(request.getName());
        customer.setDob(request.getDob());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setEmail(request.getEmail());
        Customer saved = customerRepository.save(customer);
        auditLogService.log(actorEmail, "UPDATE", "CUSTOMER", saved.getId(), "Updated customer " + saved.getName());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CustomerResponse updateOwnProfile(String email, CustomerSelfUpdateRequest request) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for current user"));
        customer.setName(request.getName());
        customer.setDob(request.getDob());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        Customer saved = customerRepository.save(customer);
        auditLogService.log(email, "UPDATE", "CUSTOMER", saved.getId(), "Customer self-updated their profile");
        return toResponse(saved);
    }

    @Override
    public CustomerResponse getById(Long id, String requestingAgentEmail) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        assertOwnership(customer, requestingAgentEmail);
        return toResponse(customer);
    }

    @Override
    public CustomerResponse getByCurrentUser(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for current user"));
        return toResponse(customer);
    }

    @Override
    public List<CustomerResponse> getAll(String requestingAgentEmail) {
        return customerRepository.findScoped(requestingAgentEmail, null).stream().map(this::toResponse).toList();
    }

    @Override
    public List<CustomerResponse> search(String keyword, String requestingAgentEmail) {
        return customerRepository.findScoped(requestingAgentEmail, keyword).stream().map(this::toResponse).toList();
    }

    @Override
    public PageResponse<CustomerResponse> getAllPaged(String requestingAgentEmail, Pageable pageable) {
        Page<Customer> page = customerRepository.findScoped(requestingAgentEmail, null, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    public PageResponse<CustomerResponse> searchPaged(String keyword, String requestingAgentEmail, Pageable pageable) {
        Page<Customer> page = customerRepository.findScoped(requestingAgentEmail, keyword, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id, String actorEmail) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        customerRepository.deleteById(id);
        auditLogService.log(actorEmail, "DELETE", "CUSTOMER", id, "Deleted customer " + customer.getName());
    }

    @Override
    @Transactional
    public CustomerResponse assignAgent(Long customerId, Long agentId, String actorEmail) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        if (agentId == null) {
            customer.setRegisteredByAgentEmail(null);
            Customer saved = customerRepository.save(customer);
            auditLogService.log(actorEmail, "UNASSIGN", "CUSTOMER", saved.getId(),
                    "Unassigned customer " + saved.getName() + " (returned to unassigned pool)");
            return toResponse(saved);
        }

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + agentId));
        if (agent.getRole() != Role.AGENT && agent.getRole() != Role.ADMIN) {
            throw new BadRequestException("Customers can only be assigned to Agent or Admin users");
        }

        customer.setRegisteredByAgentEmail(agent.getEmail());
        Customer saved = customerRepository.save(customer);
        auditLogService.log(actorEmail, "ASSIGN", "CUSTOMER", saved.getId(),
                "Assigned customer " + saved.getName() + " to " + agent.getName());
        return toResponse(saved);
    }

    private void assertOwnership(Customer customer, String requestingAgentEmail) {
        if (requestingAgentEmail == null) {
            return;
        }
        boolean owns = requestingAgentEmail.equalsIgnoreCase(customer.getRegisteredByAgentEmail());
        if (!owns) {
            throw new AccessDeniedException("You can only access customers you registered");
        }
    }

    private CustomerResponse toResponse(Customer customer) {
        Long assignedAgentId = null;
        String assignedAgentName = null;
        if (customer.getRegisteredByAgentEmail() != null) {
            Optional<User> agent = userRepository.findByEmail(customer.getRegisteredByAgentEmail());
            if (agent.isPresent()) {
                assignedAgentId = agent.get().getId();
                assignedAgentName = agent.get().getName();
            }
        }

        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .dob(customer.getDob())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .email(customer.getEmail())
                .assignedAgentId(assignedAgentId)
                .assignedAgentName(assignedAgentName)
                .createdAt(customer.getCreatedAt())
                .build();
    }
}