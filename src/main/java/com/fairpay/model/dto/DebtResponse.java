package com.fairpay.model.dto;

import java.math.BigDecimal;

public record DebtResponse(UserResponse from, UserResponse to, BigDecimal amount) {
}
