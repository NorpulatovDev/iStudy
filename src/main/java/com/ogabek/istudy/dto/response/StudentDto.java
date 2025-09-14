package com.ogabek.istudy.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class StudentDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Long branchId;
    private String branchName;
    private LocalDateTime createdAt;

    // NEW: List of groups the student belongs to
    private List<GroupInfo> groups = new ArrayList<>();

    // Payment status fields
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

    // Inner class for group information
    @Getter @Setter
    public static class GroupInfo {
        private Long id;
        private String name;
        private Long courseId;
        private String courseName;
        private String teacherName;

        public GroupInfo() {}

        public GroupInfo(Long id, String name, Long courseId, String courseName, String teacherName) {
            this.id = id;
            this.name = name;
            this.courseId = courseId;
            this.courseName = courseName;
            this.teacherName = teacherName;
        }
    }
}