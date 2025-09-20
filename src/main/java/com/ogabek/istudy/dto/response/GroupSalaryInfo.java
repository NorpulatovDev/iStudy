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
    private int studentCount;
    private BigDecimal totalGroupPayments;
}