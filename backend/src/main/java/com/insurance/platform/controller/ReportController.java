package com.insurance.platform.controller;

import com.insurance.platform.dto.ReportSummaryResponse;
import com.insurance.platform.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports Dashboard", description = "Business summary statistics and downloadable PDF reports")
@PreAuthorize("hasAnyRole('ADMIN','AGENT')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryResponse> getSummary() {
        return ResponseEntity.ok(reportService.getSummary());
    }

    @GetMapping(value = "/monthly-report/pdf")
    public ResponseEntity<byte[]> downloadMonthlyReportPdf() {
        byte[] pdfBytes = reportService.generateMonthlyReportPdf();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"monthly-business-report.pdf\"")
                .body(pdfBytes);
    }
}
