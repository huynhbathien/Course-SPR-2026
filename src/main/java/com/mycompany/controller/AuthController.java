package com.mycompany.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.mycompany.dto.APIResponse;
import com.mycompany.dto.request.LoginRequestDTO;
import com.mycompany.dto.request.RegisterRequestDTO;
import com.mycompany.enums.EnumAuthError;
import com.mycompany.enums.EnumSuccess;
import com.mycompany.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthController {

    AuthService authService;

    String refreshTokenPath = "/auth/refresh";
    @Value("${token.refresh-token-expiration:604800}")
    long refreshTokenExpirationSeconds; // in seconds (7 days default)

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    public void setCookie(HttpServletResponse response, String name, String value, int maxAge, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path(path)
                .maxAge(maxAge)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    @PostMapping("/login")
    public APIResponse<Object> login(@Valid @RequestBody LoginRequestDTO authRequestDTO,
            HttpServletRequest request, HttpServletResponse response) {
        String clientIp = resolveClientIp(request);
        HashMap<String, String> data = authService.login(authRequestDTO, clientIp);
        setCookie(
                response,
                "refreshToken",
                data.get("refreshToken"),
                (int) refreshTokenExpirationSeconds,
                refreshTokenPath);

        return APIResponse.success(EnumSuccess.LOGIN_SUCCESS.getCode(),
                EnumSuccess.LOGIN_SUCCESS.getMessage(),
                data);

    }

    @PostMapping("/register")
    public APIResponse<Object> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO,
            HttpServletResponse response) {
        HashMap<String, String> data = authService.register(registerRequestDTO);
        setCookie(
                response,
                "refreshToken",
                data.get("refreshToken"),
                (int) refreshTokenExpirationSeconds,
                refreshTokenPath);
        return APIResponse.success(EnumSuccess.REGISTRATION_SUCCESS.getCode(),
                EnumSuccess.REGISTRATION_SUCCESS.getMessage(),
                data);
    }

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

            // Delegate logic to service
            Map<String, Object> response = authService.refreshToken(username);

            return APIResponse.success(EnumSuccess.LOGIN_SUCCESS.getCode(),
                    EnumSuccess.LOGIN_SUCCESS.getMessage(), response);
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage(), e);
            return APIResponse.error(EnumAuthError.INTERNAL_ERROR.getCode(),
                    EnumAuthError.INTERNAL_ERROR.getMessage(), EnumAuthError.INTERNAL_ERROR.name());
        }
    }

    @PostMapping("/logout")
    public APIResponse<Object> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return APIResponse.error(EnumAuthError.UNAUTHORIZED.getCode(),
                        EnumAuthError.UNAUTHORIZED.getMessage(), EnumAuthError.UNAUTHORIZED.name());
            }

            String username = authentication.getName();

            // Delegate logic to service
            authService.logout(username);

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
