package com.fairpay.model.mapper;

import com.fairpay.model.dto.UserResponse;
import com.fairpay.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
}
