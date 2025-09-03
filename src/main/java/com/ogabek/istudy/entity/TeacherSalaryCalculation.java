package com.ogabek.istudy.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @ManyToOne
    @Column(name = "teacher_id", nullable = false)
    private Teacher teacher;

    private int year;
    private int month;
    private int day;

    private BigDecimal baseSalary;
    private BigDecimal paymentBasedSalary;
    private BigDecimal totalSalary;

    private BigDecimal totalStudentPayments;
    private int totalStudents;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Enumerated(EnumType.STRING)
    private SalaryType salaryType;

    @CreationTimestamp
    private LocalDateTime createdAt;


}
