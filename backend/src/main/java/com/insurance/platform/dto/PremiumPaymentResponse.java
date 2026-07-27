package com.insurance.platform.dto;

import com.insurance.platform.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumPaymentResponse {
    private Long id;
    private Long policyId;
    private String policyNumber;
    private String customerName;
    private LocalDate paymentDate;
    private LocalDate dueDate;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
}
