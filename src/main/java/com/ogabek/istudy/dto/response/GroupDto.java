package com.ogabek.istudy.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class GroupDto {
    private Long id;
    private String name;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private Long branchId;
    private String branchName;
    private List<StudentDto> students;
    private LocalDateTime createdAt;

    // UPDATED: Return time as String
    private String startTime;  // Returns "13:00", "09:30", etc.
    private String endTime;    // Returns "15:00", "11:30", etc.
    private List<String> daysOfWeek; // ["MONDAY", "WEDNESDAY", "FRIDAY"]
}