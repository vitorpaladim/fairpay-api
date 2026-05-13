package com.fairpay.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ExpenseResponse(
    Long id,
    Long groupId,
    UserResponse paidBy,
    String description,
    BigDecimal totalAmount,
    LocalDate date,
    Instant createdAt,
    List<ExpenseSplitResponse> splits
) {
}
