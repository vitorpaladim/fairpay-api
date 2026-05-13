package com.fairpay.model.dto;

import java.util.List;

public record BalanceResponse(Long groupId, List<DebtResponse> debts) {
}
