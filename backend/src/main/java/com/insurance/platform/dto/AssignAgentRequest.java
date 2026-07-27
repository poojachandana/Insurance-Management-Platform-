package com.insurance.platform.dto;

import lombok.Data;

@Data
public class AssignAgentRequest {
    /** The Agent (or Admin) to assign this customer to. Pass null to unassign (send back to the pool). */
    private Long agentId;
}