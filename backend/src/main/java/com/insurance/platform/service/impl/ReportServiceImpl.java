package com.insurance.platform.service.impl;

import com.insurance.platform.dto.ReportSummaryResponse;
import com.insurance.platform.entity.ClaimStatus;
import com.insurance.platform.entity.PaymentStatus;
import com.insurance.platform.entity.PolicyStatus;
import com.insurance.platform.repository.ClaimRepository;
import com.insurance.platform.repository.CustomerRepository;
import com.insurance.platform.repository.PolicyRepository;
import com.insurance.platform.repository.PremiumPaymentRepository;
import com.insurance.platform.service.ReportService;
import com.insurance.platform.util.PdfReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final PremiumPaymentRepository premiumPaymentRepository;
    private final CustomerRepository customerRepository;
    private final PdfReportGenerator pdfReportGenerator;

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public ReportSummaryResponse getSummary() {
        long activePolicies = policyRepository.countByStatus(PolicyStatus.ACTIVE);
        long expiredPolicies = policyRepository.countByStatus(PolicyStatus.EXPIRED);
        long cancelledPolicies = policyRepository.countByStatus(PolicyStatus.CANCELLED);
        long totalCustomers = customerRepository.count();

        long pendingClaims = claimRepository.countByStatus(ClaimStatus.PENDING);
        long approvedClaims = claimRepository.countByStatus(ClaimStatus.APPROVED);
        long rejectedClaims = claimRepository.countByStatus(ClaimStatus.REJECTED);

        BigDecimal totalPremiumCollected = premiumPaymentRepository.findByPaymentStatus(PaymentStatus.PAID)
                .stream().map(p -> p.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalClaimApproved = claimRepository.findByStatus(ClaimStatus.APPROVED)
                .stream().map(c -> c.getClaimAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> customerGrowth = new TreeMap<>();
        customerRepository.findAll().forEach(c -> {
            if (c.getCreatedAt() != null) {
                String key = c.getCreatedAt().format(MONTH_FORMAT);
                customerGrowth.merge(key, 1L, Long::sum);
            }
        });

        Map<String, BigDecimal> premiumByMonth = new TreeMap<>();
        premiumPaymentRepository.findByPaymentStatus(PaymentStatus.PAID).forEach(p -> {
            if (p.getPaymentDate() != null) {
                String key = p.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                premiumByMonth.merge(key, p.getAmount(), BigDecimal::add);
            }
        });

        List<Map<String, Object>> monthlyReport = buildMonthlyBusinessReport(customerGrowth, premiumByMonth);

        return ReportSummaryResponse.builder()
                .activePolicies(activePolicies)
                .expiredPolicies(expiredPolicies)
                .cancelledPolicies(cancelledPolicies)
                .totalCustomers(totalCustomers)
                .pendingClaims(pendingClaims)
                .approvedClaims(approvedClaims)
                .rejectedClaims(rejectedClaims)
                .totalPremiumCollected(totalPremiumCollected)
                .totalClaimAmountApproved(totalClaimApproved)
                .customerGrowthByMonth(customerGrowth)
                .premiumCollectionByMonth(premiumByMonth)
                .monthlyBusinessReport(monthlyReport)
                .build();
    }

    private List<Map<String, Object>> buildMonthlyBusinessReport(Map<String, Long> customerGrowth,
                                                                  Map<String, BigDecimal> premiumByMonth) {
        java.util.Set<String> months = new java.util.TreeSet<>();
        months.addAll(customerGrowth.keySet());
        months.addAll(premiumByMonth.keySet());

        return months.stream().map(month -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", month);
            row.put("newCustomers", customerGrowth.getOrDefault(month, 0L));
            row.put("premiumCollected", premiumByMonth.getOrDefault(month, BigDecimal.ZERO));
            return row;
        }).toList();
    }

    @Override
    public byte[] generateMonthlyReportPdf() {
        ReportSummaryResponse summary = getSummary();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("Active Policies", summary.getActivePolicies());
        stats.put("Expired Policies", summary.getExpiredPolicies());
        stats.put("Cancelled Policies", summary.getCancelledPolicies());
        stats.put("Total Customers", summary.getTotalCustomers());
        stats.put("Pending Claims", summary.getPendingClaims());
        stats.put("Approved Claims", summary.getApprovedClaims());
        stats.put("Rejected Claims", summary.getRejectedClaims());
        stats.put("Total Premium Collected", summary.getTotalPremiumCollected());
        stats.put("Total Claim Amount Approved", summary.getTotalClaimAmountApproved());
        return pdfReportGenerator.generateBusinessReport(stats);
    }
}
