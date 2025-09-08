package com.ogabek.istudy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_salary_calculations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSalaryCalculation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    private int year;
    private int month;

    @Column(precision = 10, scale = 2)
    private BigDecimal baseSalary;

    @Column(precision = 10, scale = 2)
    private BigDecimal paymentBasedSalary;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalSalary;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalStudentPayments;

    private int totalStudents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    private SalaryCalculationStatus status = SalaryCalculationStatus.CALCULATED;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
