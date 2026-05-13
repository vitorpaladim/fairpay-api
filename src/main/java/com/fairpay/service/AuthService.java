package com.fairpay.service;

import com.fairpay.config.JwtService;
import com.fairpay.exception.BusinessException;
import com.fairpay.model.dto.AuthResponse;
import com.fairpay.model.dto.LoginRequest;
import com.fairpay.model.dto.RegisterRequest;
import com.fairpay.model.entity.User;
import com.fairpay.model.mapper.UserMapper;
import com.fairpay.repository.UserRepository;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email already registered");
        }

        User user = new User(
            request.name().trim(),
            email,
            passwordEncoder.encode(request.password())
        );

        userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user.getEmail()), userMapper.toResponse(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, request.password())
        );

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("Invalid credentials"));

        return new AuthResponse(jwtService.generateToken(user.getEmail()), userMapper.toResponse(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
