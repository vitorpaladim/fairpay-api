package com.fairpay.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GroupMemberAddRequest(@NotNull @Positive Long userId) {
}
