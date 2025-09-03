package com.ogabek.istudy.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class CourseDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int durationMonths;
    private Long branchId;
    private String branchName;
    private LocalDateTime createdAt;

}
