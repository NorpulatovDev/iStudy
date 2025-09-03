package com.ogabek.istudy.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryCalculationRequest {
    @NotNull(message = "Yil majburiy")
    @Min(value = 2020, message = "Yil 2024 dan baland bo'lishi kerak")
    private Integer year;

    @NotNull(message = "Oy majburiy")
    @Min(value = 1, message = "Oy 1-12 oralig'ida bo'lishi kerak")
    @Max(value = 12, message = "Oy 1-12 oralig'ida bo'lishi kerak")
    private Integer month;
}
