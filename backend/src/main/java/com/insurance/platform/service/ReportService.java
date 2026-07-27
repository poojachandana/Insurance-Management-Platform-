package com.insurance.platform.service;

import com.insurance.platform.dto.ReportSummaryResponse;

public interface ReportService {
    ReportSummaryResponse getSummary();
    byte[] generateMonthlyReportPdf();
}
