package com.ogabek.istudy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentPaymentInfo {
    private Long studentId;
    private String studentName;
    private BigDecimal totalPaidInMonth;
    private BigDecimal coursePrice;
    private BigDecimal remainingAmount;
    private String paymentStatus; // "PAID", "PARTIAL", "UNPAID"
}