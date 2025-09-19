package com.ogabek.istudy.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateSalaryPaymentRequest {
    @NotNull(message = "O'qituvchi majburiy")
    private Long teacherId;

    @NotNull(message = "Yil majburiy")
    @Min(value = 2020, message = "Yil 2020 dan kichik bo'lmasligi kerak")
    private Integer year;

    @NotNull(message = "Oy majburiy")
    @Min(value = 1, message = "Oy 1-12 oralig'ida bo'lishi kerak")
    @Max(value = 12, message = "Oy 1-12 oralig'ida bo'lishi kerak")
    private Integer month;

    @NotNull(message = "Miqdor majburiy")
    @DecimalMin(value = "0.0", inclusive = false, message = "Miqdor 0 dan katta bo'lishi kerak")
    private BigDecimal amount;

    @Size(max = 255, message = "Tavsif 255 harfdan kam bo'lishi kerak")
    private String description;

    @NotNull(message = "Filial majburiy")
    private Long branchId;
}