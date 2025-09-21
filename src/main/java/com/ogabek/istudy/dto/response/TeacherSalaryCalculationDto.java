package com.ogabek.istudy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSalaryCalculationDto {
    private Long teacherId;
    private String teacherName;
    private int year;
    private int month;
    private BigDecimal baseSalary;
    private BigDecimal paymentBasedSalary;
    private BigDecimal totalSalary;
    private BigDecimal totalStudentPayments;
    private int totalStudents;
    private BigDecimal alreadyPaid;
    private BigDecimal remainingAmount;
    private Long branchId;
    private String branchName;

    // Enhanced: Group information for detailed breakdown
    private List<GroupSalaryInfo> groups = new ArrayList<>();

    // Constructor without groups for backward compatibility
    public TeacherSalaryCalculationDto(Long teacherId, String teacherName, int year, int month,
                                     BigDecimal baseSalary, BigDecimal paymentBasedSalary, BigDecimal totalSalary,
                                     BigDecimal totalStudentPayments, int totalStudents, BigDecimal alreadyPaid,
                                     BigDecimal remainingAmount, Long branchId, String branchName) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.year = year;
        this.month = month;
        this.baseSalary = baseSalary;
        this.paymentBasedSalary = paymentBasedSalary;
        this.totalSalary = totalSalary;
        this.totalStudentPayments = totalStudentPayments;
        this.totalStudents = totalStudents;
        this.alreadyPaid = alreadyPaid;
        this.remainingAmount = remainingAmount;
        this.branchId = branchId;
        this.branchName = branchName;
        this.groups = new ArrayList<>();
    }
}