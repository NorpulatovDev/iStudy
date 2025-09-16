package com.ogabek.istudy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

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

    // UPDATED: Accept time as string (HH:MM format)
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Start time must be in HH:MM format (e.g., 13:00)")
    private String startTime; // "13:00", "09:30", etc.

    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "End time must be in HH:MM format (e.g., 15:00)")
    private String endTime;   // "15:00", "11:30", etc.

    private List<String> daysOfWeek = new ArrayList<>(); // ["MONDAY", "WEDNESDAY", "FRIDAY"]
}