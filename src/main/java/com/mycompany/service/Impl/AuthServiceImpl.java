package com.mycompany.service.Impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mycompany.dto.request.RegisterRequestDTO;
import com.mycompany.entity.UserEntity;
import com.mycompany.enums.EnumAuthError;
import com.mycompany.mapstruct.UserMapper;
import com.mycompany.repository.UserRepository;
import com.mycompany.security.JwtUtils;
import com.mycompany.service.AuthService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtUtils jwtUtils;
    UserMapper userMapper;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
    }

    @Override
    public String login(com.mycompany.dto.request.LoginRequestDTO authRequestDTO) {
        String username = authRequestDTO.getUsername();
        String password = userRepository.findByUsername(username).get().getPassword();
        if (!passwordEncoder.matches(authRequestDTO.getPassword(), password)) {
            throw new RuntimeException(EnumAuthError.INVALID_CREDENTIALS.getMessage());
        }
        String token = jwtUtils.generateToken(username);
        return token;
    }

    @Override
    @Transactional
    public String register(@Valid RegisterRequestDTO registerRequestDTO) {
        String username = registerRequestDTO.getUsername();
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException(EnumAuthError.USER_ALREADY_EXISTS.getMessage());
        }
        if (!registerRequestDTO.getPassword().equals(registerRequestDTO.getConfirmPassword())) {
            throw new RuntimeException(EnumAuthError.PASSWORD_MISMATCH.getMessage());
        }
        UserEntity user = userMapper.toUserEntity(registerRequestDTO);
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user.setRole("ROLE_USER");
        userRepository.save(user);
        log.info("User {} registered successfully", username);
        String token = jwtUtils.generateToken(username);
        return token;
    }

}
