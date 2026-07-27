package com.insurance.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryResponse {
    private long activePolicies;
    private long expiredPolicies;
    private long cancelledPolicies;
    private long totalCustomers;
    private long pendingClaims;
    private long approvedClaims;
    private long rejectedClaims;
    private BigDecimal totalPremiumCollected;
    private BigDecimal totalClaimAmountApproved;
    private Map<String, Long> customerGrowthByMonth;
    private Map<String, BigDecimal> premiumCollectionByMonth;
    private List<Map<String, Object>> monthlyBusinessReport;
}
