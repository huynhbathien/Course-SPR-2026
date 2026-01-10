package com.mycompany.service.Impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mycompany.dto.request.RegisterRequestDTO;
import com.mycompany.enums.EnumAuthError;
import com.mycompany.mapper.MapStruct;
import com.mycompany.repository.UserRepository;
import com.mycompany.security.JwtUtils;
import com.mycompany.service.AuthService;

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
    MapStruct mapStruct;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils,
            MapStruct mapStruct) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.mapStruct = mapStruct;
    }

    @Override
    public String login(com.mycompany.dto.request.LoginRequestDTO authRequestDTO) {
        String username = authRequestDTO.getUsername();
        String password = userRepository.findByUsername(username).get().getPassword();
        if (!passwordEncoder.matches(authRequestDTO.getPassword(), password)) {
            throw new RuntimeException(EnumAuthError.INVALID_CREDENTIALS.getMessage());
        }
        String token = jwtUtils.generateToken(username);
        log.info("User {} logged in successfully", username);
        return token;
    }

    @Override
    public String register(RegisterRequestDTO registerRequestDTO) {
        String username = registerRequestDTO.getUsername();
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException(EnumAuthError.USER_ALREADY_EXISTS.getMessage());
        }
        mapStruct.toUserEntity(registerRequestDTO);
        log.info("User {} registered successfully", username);
        String token = jwtUtils.generateToken(username);
        return token;
    }

}
