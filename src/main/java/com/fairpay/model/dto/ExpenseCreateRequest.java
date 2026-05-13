package com.fairpay.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseCreateRequest(
    @NotNull @Positive Long groupId,
    @NotBlank @Size(max = 180) String description,
    @NotNull @Positive BigDecimal totalAmount,
    @NotNull LocalDate date
) {
}
