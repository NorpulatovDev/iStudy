package com.ogabek.istudy.dto.request;

import com.ogabek.istudy.entity.SalaryType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class CreateTeacherRequest {
    @NotBlank(message = "Ism kiritish majburiy")
    @Size(min = 2, max = 50, message = "Ism 2-50 harfdan iborat bo'lishi shart")
    private String firstName;

    @NotBlank(message = "Familiya kiritish majburiy")
    @Size(min = 2, max = 50, message = "Familiya 2-50 harfdan iborat bo'lishi shart")
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Noto'gri formatdagi telefon raqam")
    private String phoneNumber;

    @DecimalMin(value = "0.0", inclusive = false, message = "Asosiy maosh 0 dan katta bo'lishi kerak")
    private BigDecimal baseSalary = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "To'lov foizi 0-100% oralig'ida bo'lishi kerak!")
    @DecimalMax(value = "100.0", message = "To'lov foizi 0-100% oralig'ida bo'lishi kerak!")
    private BigDecimal paymentPercentage = BigDecimal.ZERO;

    @NotNull(message = "Maosh turini kiritish majburiy")
    private SalaryType salaryType;

    @NotNull(message = "Filial kiritish majburiy")
    private Long branchId;
}
