package com.fairpay.service;

import com.fairpay.exception.ResourceNotFoundException;
import com.fairpay.model.dto.UserResponse;
import com.fairpay.model.entity.User;
import com.fairpay.model.mapper.UserMapper;
import com.fairpay.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(String email) {
        return userMapper.toResponse(getByEmail(email));
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
