package com.ogabek.istudy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class CreateGroupRequest {
    @NotBlank(message = "Guruh nomi majburiy")
    @Size(min = 2, max = 50, message = "Guruh nomi 2-50 harfdan iborat bo'lishi kerak")
    private String name;

    @NotNull(message = "Kurs majburiy")
    private Long courseId;

    @NotNull(message = "O'qituvchi majburiy")
    private Long teacherId;

    @NotNull(message = "Filial majburiy")
    private Long branchId;

    private List<Long> studentIds = new ArrayList<>();

    // NEW: Schedule fields
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> daysOfWeek = new ArrayList<>(); // ["MONDAY", "WEDNESDAY", "FRIDAY"]
}