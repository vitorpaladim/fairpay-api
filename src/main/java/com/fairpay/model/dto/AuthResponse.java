package com.fairpay.model.dto;

public record AuthResponse(String token, UserResponse user) {
}
