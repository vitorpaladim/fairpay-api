package com.fairpay.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupCreateRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 500) String description
) {
}
