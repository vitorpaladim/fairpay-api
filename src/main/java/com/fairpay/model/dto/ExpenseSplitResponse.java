package com.fairpay.model.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ExpenseSplitResponse(
    Long id,
    UserResponse user,
    BigDecimal amountOwed,
    boolean settled,
    Instant settledAt
) {
}
