package com.ogabek.istudy.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class CreatePaymentRequest {
    @NotNull(message = "O'quvchi majburiy")
    private Long studentId;

    @NotNull(message = "Kurs majburiy")
    private Long courseId;

    @NotNull(message = "Miqdor majburiy")
    @DecimalMin(value = "0.0", inclusive = false, message = "Miqdor 0 dan katta bo'lishi kerak")
    private BigDecimal amount;

    @Size(max = 255, message = "Tavsif 255 harfdan kam bo'lishi kerak")
    private String description;

    @NotNull(message = "Filial majburiy")
    private Long branchId;
}
