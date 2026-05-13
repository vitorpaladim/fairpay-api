package com.fairpay.model.dto;

import java.time.Instant;
import java.util.List;

public record GroupResponse(
    Long id,
    String name,
    String description,
    Instant createdAt,
    UserResponse owner,
    List<GroupMemberResponse> members
) {
}
