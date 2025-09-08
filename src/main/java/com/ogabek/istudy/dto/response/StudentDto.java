package com.ogabek.istudy.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class StudentDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Long branchId;
    private String branchName;
    private LocalDateTime createdAt;

    // NEW: Payment status fields
    private Boolean hasPaidInMonth;
    private BigDecimal totalPaidInMonth;
    private BigDecimal remainingAmount;
    private String paymentStatus; // "PAID", "PARTIAL", "UNPAID"
    private LocalDateTime lastPaymentDate;

    // Constructor for basic student info
    public StudentDto() {}

    public StudentDto(Long id, String firstName, String lastName, String phoneNumber,
                      Long branchId, String branchName, LocalDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.branchId = branchId;
        this.branchName = branchName;
        this.createdAt = createdAt;
    }
}