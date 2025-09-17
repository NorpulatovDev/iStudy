package com.ogabek.istudy.dto.response;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class PaymentDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private Long groupId; // NEW
    private String groupName; // NEW
    private BigDecimal amount;
    private String description;
    private String status;
    private Long branchId;
    private String branchName;
    private LocalDateTime createdAt;
}
