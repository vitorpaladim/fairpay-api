package com.fairpay.model.dto;

import com.fairpay.model.entity.GroupMemberRole;
import java.time.Instant;

public record GroupMemberResponse(UserResponse user, GroupMemberRole role, Instant joinedAt) {
}
