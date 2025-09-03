package com.ogabek.istudy.dto.response;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TeacherSalaryCalculationDto {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private int year;
    private int month;
    private BigDecimal baseSalary;
    private BigDecimal paymentBasedSalary;
    private BigDecimal totalSalary;
    private BigDecimal totalStudentPayments;
    private int totalStudents;
    private String status;
    private Long branchId;
    private String branchName;
    private LocalDateTime createdAt;
}
