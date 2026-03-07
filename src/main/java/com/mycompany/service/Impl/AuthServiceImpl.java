package com.mycompany.service.Impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mycompany.dto.request.LoginRequestDTO;
import com.mycompany.dto.request.RegisterRequestDTO;
import com.mycompany.entity.UserEntity;
import com.mycompany.enums.EnumAuthError;
import com.mycompany.mapstruct.UserMapper;
import com.mycompany.repository.UserRepository;
import com.mycompany.security.JwtUtils;
import com.mycompany.security.LoginAttemptService;
import com.mycompany.service.AuthService;
import com.mycompany.service.TokenRedisService;

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
    TokenRedisService tokenRedisService;
    LoginAttemptService loginAttemptService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils,
            UserMapper userMapper, TokenRedisService tokenRedisService,
            LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
        this.tokenRedisService = tokenRedisService;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public HashMap<String, String> login(LoginRequestDTO authRequestDTO, String clientIp) {
        // Brute-force check
        if (loginAttemptService.isBlocked(clientIp)) {
            log.warn("Blocked login attempt from IP: {}", clientIp);
            throw new RuntimeException(EnumAuthError.TOO_MANY_REQUESTS.getMessage());
        }

        String username = authRequestDTO.getUsername();
        UserEntity user;
        try {
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException(EnumAuthError.USER_NOT_FOUND.getMessage()));
        } catch (RuntimeException e) {
            loginAttemptService.loginFailed(clientIp);
            throw e;
        }

        if (!passwordEncoder.matches(authRequestDTO.getPassword(), user.getPassword())) {
            loginAttemptService.loginFailed(clientIp);
            int remaining = loginAttemptService.getRemainingAttempts(clientIp);
            log.warn("Invalid credentials for user '{}' from IP '{}'. Remaining attempts: {}",
                    username, clientIp, remaining);
            throw new RuntimeException(EnumAuthError.INVALID_CREDENTIALS.getMessage());
        }

        // Success – clear attempt counter
        loginAttemptService.loginSucceeded(clientIp);

        String token = jwtUtils.generateToken(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);

        tokenRedisService.saveAccessToken(username, user.getId(), token);
        tokenRedisService.saveRefreshToken(user.getUsername(), user.getId(), refreshToken);

        HashMap<String, String> hashMapToken = new HashMap<>();
        hashMapToken.put("token", token);
        hashMapToken.put("refreshToken", refreshToken);

        return hashMapToken;
    }

    @Override
    @Transactional
    public HashMap<String, String> register(@Valid RegisterRequestDTO registerRequestDTO) {
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

        String refreshToken = jwtUtils.generateRefreshToken(username);
        tokenRedisService.saveAccessToken(username, user.getId(), token);
        tokenRedisService.saveRefreshToken(user.getUsername(), user.getId(), refreshToken);

        HashMap<String, String> hashMapToken = new HashMap<>();
        hashMapToken.put("token", token);
        hashMapToken.put("refreshToken", refreshToken);

        return hashMapToken;
    }

    @Override
    public Map<String, Object> refreshToken(String username) {
        log.info("Refresh token request for user: {}", username);

        // Kiểm tra refresh token trong Redis
        String refreshToken = tokenRedisService.getRefreshToken(username);
        if (refreshToken == null) {
            log.warn("Refresh token not found in Redis for user: {}", username);
            throw new RuntimeException(EnumAuthError.REFRESH_TOKEN_NOT_FOUND.getMessage());
        }

        // Validate refresh token
        if (jwtUtils.isTokenExpired(refreshToken)) {
            log.warn("Refresh token expired for user: {}", username);
            tokenRedisService.deleteRefreshToken(username);
            throw new RuntimeException(EnumAuthError.REFRESH_TOKEN_EXPIRED.getMessage());
        }

        // Generate new access token
        String newAccessToken = jwtUtils.generateToken(username);
        log.info("New access token generated for user: {}", username);

        // Rotate refresh token - generate new one and update Redis
        String newRefreshToken = jwtUtils.generateRefreshToken(username);
        Long userId = tokenRedisService.getUserId(username);
        tokenRedisService.saveRefreshToken(username, userId, newRefreshToken);
        log.info("Refresh token rotated for user: {}", username);

        // Prepare response with new tokens
        Map<String, Object> response = new HashMap<>();
        response.put("token", newAccessToken);
        response.put("refreshToken", newRefreshToken);
        response.put("username", username);
        response.put("message", "Access token refreshed successfully with new refresh token");

        return response;
    }

    @Override
    public void logout(String username) {
        log.info("Logout request for user: {}", username);
        tokenRedisService.deleteRefreshToken(username);
        log.info("Refresh token deleted for user: {}", username);
    }
}
