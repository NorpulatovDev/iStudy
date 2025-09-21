package com.ogabek.istudy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupSalaryInfo {
    private Long groupId;
    private String groupName;
    private String courseName;
    private int studentCount;           // Total students who paid in this group for the month
    private BigDecimal totalGroupPayments;  // Total payments for this group in the month

    // Additional fields for more detailed information
    private int totalStudentsInGroup;   // Total students enrolled in group (regardless of payment)
    private BigDecimal coursePrice;     // Course price for reference

    // Constructor for backward compatibility
    public GroupSalaryInfo(Long groupId, String groupName, String courseName,
                           int studentCount, BigDecimal totalGroupPayments) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.courseName = courseName;
        this.studentCount = studentCount;
        this.totalGroupPayments = totalGroupPayments;
        this.totalStudentsInGroup = studentCount; // Default to same value
        this.coursePrice = BigDecimal.ZERO;
    }
}