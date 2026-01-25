package com.mycompany.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.dto.request.APIResponse;
import com.mycompany.dto.request.LoginRequestDTO;
import com.mycompany.dto.request.RegisterRequestDTO;
import com.mycompany.enums.EnumSuccess;
import com.mycompany.enums.EnumAuthError;
import com.mycompany.security.JwtUtils;
import com.mycompany.service.AuthService;
import com.mycompany.service.TokenRedisService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthController {

    AuthService authService;
    JwtUtils jwtUtils;
    TokenRedisService tokenRedisService;

    @PostMapping("/login")
    public APIResponse<Object> login(@RequestBody LoginRequestDTO authRequestDTO) {
        String data = authService.login(authRequestDTO);
        return APIResponse.success(EnumSuccess.LOGIN_SUCCESS.getCode(),
                EnumSuccess.LOGIN_SUCCESS.getMessage(),
                data);

    }

    @PostMapping("/register")
    public APIResponse<Object> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        String data = authService.register(registerRequestDTO);
        return APIResponse.success(EnumSuccess.REGISTRATION_SUCCESS.getCode(),
                EnumSuccess.REGISTRATION_SUCCESS.getMessage(),
                data);
    }

    /**
     * Refresh access token dùng refresh token từ Redis
     * Yêu cầu: User phải authenticated
     * 
     * @return access token mới
     */
    @PostMapping("/refresh")
    public APIResponse<Object> refreshToken() {
        try {
            // Lấy current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return APIResponse.error(EnumAuthError.UNAUTHORIZED.getCode(),
                        EnumAuthError.UNAUTHORIZED.getMessage(),
                        EnumAuthError.UNAUTHORIZED.name());
            }

            String username = authentication.getName();
            log.info("Refresh token request for user: {}", username);

            // Kiểm tra refresh token trong Redis
            String refreshToken = tokenRedisService.getRefreshToken(username);
            if (refreshToken == null) {
                log.warn("Refresh token not found in Redis for user: {}", username);
                return APIResponse.error(EnumAuthError.REFRESH_TOKEN_NOT_FOUND.getCode(),
                        EnumAuthError.REFRESH_TOKEN_NOT_FOUND.getMessage(),
                        EnumAuthError.REFRESH_TOKEN_NOT_FOUND.name());
            }

            // Validate refresh token
            if (jwtUtils.isTokenExpired(refreshToken)) {
                log.warn("Refresh token expired for user: {}", username);
                tokenRedisService.deleteRefreshToken(username);
                return APIResponse.error(EnumAuthError.REFRESH_TOKEN_EXPIRED.getCode(),
                        EnumAuthError.REFRESH_TOKEN_EXPIRED.getMessage(),
                        EnumAuthError.REFRESH_TOKEN_EXPIRED.name());
            }

            // Generate new access token
            String newAccessToken = jwtUtils.generateToken(username);
            log.info("New access token generated for user: {}", username);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("token", newAccessToken);
            response.put("username", username);
            response.put("message", "Access token refreshed successfully");

            return APIResponse.success(EnumSuccess.LOGIN_SUCCESS.getCode(),
                    EnumSuccess.LOGIN_SUCCESS.getMessage(), response);
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage(), e);
            return APIResponse.error(EnumAuthError.INTERNAL_ERROR.getCode(),
                    EnumAuthError.INTERNAL_ERROR.getMessage(), EnumAuthError.INTERNAL_ERROR.name());
        }
    }

    /**
     * Logout - Xóa refresh token khỏi Redis
     * 
     * @return logout message
     */
    @PostMapping("/logout")
    public APIResponse<Object> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return APIResponse.error(EnumAuthError.UNAUTHORIZED.getCode(),
                        EnumAuthError.UNAUTHORIZED.getMessage(), EnumAuthError.UNAUTHORIZED.name());
            }

            String username = authentication.getName();
            log.info("Logout request for user: {}", username);

            // Xóa refresh token khỏi Redis
            tokenRedisService.deleteRefreshToken(username);
            log.info("Refresh token deleted for user: {}", username);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Logged out successfully");

            return APIResponse.success(EnumSuccess.SUCCESS.getCode(),
                    "Logged out successfully", response);
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            return APIResponse.error(EnumAuthError.INTERNAL_ERROR.getCode(),
                    EnumAuthError.INTERNAL_ERROR.getMessage(), EnumAuthError.INTERNAL_ERROR.name());
        }
    }

}
