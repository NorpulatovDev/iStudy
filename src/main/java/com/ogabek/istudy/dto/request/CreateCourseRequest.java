package com.ogabek.istudy.dto.request;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class CreateCourseRequest {
    @NotBlank(message = "Kurs nomi majburiy")
    @Size(min = 2, max = 100, message = "Kurs nomi 2-100 harfdan iborat bo'lishi kerak")
    private String name;

    @Size(max = 500, message = "Tavsif 500 harfdan oshmasligi kerak")
    private String description;

    @NotNull(message = "Kurs narxi majburiy")
    @DecimalMin(value = "0.0", inclusive = false, message = "Narx 0 dan katta bo'lishi keark")
    private BigDecimal price;

    private int durationMonths;

    @NotNull(message = "Filial majburiy")
    private Long branchId;
}
