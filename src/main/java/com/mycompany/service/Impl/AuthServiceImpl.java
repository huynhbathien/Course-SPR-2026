package com.mycompany.service.Impl;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import com.mycompany.dto.request.ForgotPasswordRequestDTO;
import com.mycompany.dto.request.LoginRequestDTO;
import com.mycompany.dto.request.RegisterRequestDTO;
import com.mycompany.dto.request.ResendOtpRequestDTO;
import com.mycompany.dto.request.ResetPasswordRequestDTO;
import com.mycompany.dto.request.VerifyOtpRequestDTO;
import com.mycompany.entity.UserEntity;
import com.mycompany.enums.EnumAuthError;
import com.mycompany.enums.EnumRole;
import com.mycompany.enums.OtpType;
import com.mycompany.mapstruct.UserMapper;
import com.mycompany.repository.UserRepository;
import com.mycompany.security.JwtUtils;
import com.mycompany.security.LoginAttemptService;
import com.mycompany.service.AuthService;
import com.mycompany.service.OtpService;
import com.mycompany.service.TokenRedisService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtUtils jwtUtils;
    UserMapper userMapper;
    TokenRedisService tokenRedisService;
    LoginAttemptService loginAttemptService;
    OtpService otpService;

    @Override
    public Map<String, String> login(@Valid LoginRequestDTO dto, String clientIp) {
        if (loginAttemptService.isBlocked(clientIp)) {
            log.warn("Blocked login attempt from IP: {}", clientIp);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    EnumAuthError.TOO_MANY_REQUESTS.getMessage());
        }

        String username = dto.getUsername();
        UserEntity user = userRepository.findByUsername(username).orElse(null);

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            loginAttemptService.loginFailed(clientIp);
            int remaining = loginAttemptService.getRemainingAttempts(clientIp);
            log.warn("Invalid credentials for user '{}' from IP '{}'. Remaining attempts: {}",
                    username, clientIp, remaining);
            throw new BadCredentialsException(EnumAuthError.INVALID_CREDENTIALS.getMessage());
        }

        if (!user.isEmailVerified()) {
            log.warn("Login attempt for unverified email by user '{}'", username);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    EnumAuthError.EMAIL_NOT_VERIFIED.getMessage());
        }

        loginAttemptService.loginSucceeded(clientIp);
        return issueTokenPair(user);
    }

    // --- Register ----------------------------------------------------------------

    @Override
    @Transactional
    public Map<String, String> register(@Valid RegisterRequestDTO dto) {
        String username = dto.getUsername();
        String email = dto.getEmail();

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    EnumAuthError.USER_ALREADY_EXISTS.getMessage());
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    EnumAuthError.EMAIL_ALREADY_EXISTS.getMessage());
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    EnumAuthError.PASSWORD_MISMATCH.getMessage());
        }

        UserEntity user = userMapper.toUserEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(EnumRole.USER.getRoleName());
        user.setEmailVerified(false);
        userRepository.save(user);
        log.info("User '{}' registered awaiting email verification", username);

        otpService.generateAndSendOtp(email, OtpType.EMAIL_VERIFICATION);

        return Map.of("message", "Registration successful. OTP has been sent to " + maskEmail(email));
    }

    // --- Verify email & auto-login -----------------------------------------------
    @Override
    @Transactional
    public Map<String, String> verifyEmailAndLogin(@Valid VerifyOtpRequestDTO dto) {
        otpService.verifyOtp(dto.getEmail(), dto.getOtpCode(), OtpType.EMAIL_VERIFICATION);

        UserEntity user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        EnumAuthError.USER_NOT_FOUND.getMessage()));

        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified for user '{}'", user.getUsername());

        return issueTokenPair(user);
    }

    // --- Resend OTP --------------------------------------------------------------

    @Override
    public void resendOtp(@Valid ResendOtpRequestDTO dto) {
        UserEntity user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        EnumAuthError.USER_NOT_FOUND.getMessage()));

        if (OtpType.EMAIL_VERIFICATION.equals(dto.getOtpType()) && user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already verified.");
        }

        otpService.generateAndSendOtp(dto.getEmail(), dto.getOtpType());
        log.info("OTP resent to '{}' for type={}", dto.getEmail(), dto.getOtpType());
    }

    // --- Forgot password ---------------------------------------------------------

    @Override
    public void forgotPassword(@Valid ForgotPasswordRequestDTO dto) {
        userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        EnumAuthError.USER_NOT_FOUND.getMessage()));

        otpService.generateAndSendOtp(dto.getEmail(), OtpType.FORGOT_PASSWORD);
        log.info("Forgot-password OTP sent to '{}'", dto.getEmail());
    }

    // --- Reset password ----------------------------------------------------------

    @Override
    @Transactional
    public void resetPassword(@Valid ResetPasswordRequestDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    EnumAuthError.PASSWORD_MISMATCH.getMessage());
        }

        otpService.verifyOtp(dto.getEmail(), dto.getOtpCode(), OtpType.FORGOT_PASSWORD);

        UserEntity user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        EnumAuthError.USER_NOT_FOUND.getMessage()));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        // Revoke existing sessions so the attacker can no longer use old tokens
        tokenRedisService.deleteAccessToken(user.getUsername());
        tokenRedisService.deleteRefreshToken(user.getUsername());
        log.info("Password reset successfully for user '{}'", user.getUsername());
    }

    // --- Refresh token -----------------------------------------------------------

    @Override
    public Map<String, String> refreshToken(String clientRefreshToken) {
        // 1. Verify JWT signature & expiry before any Redis lookup
        if (jwtUtils.isTokenExpired(clientRefreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    EnumAuthError.REFRESH_TOKEN_EXPIRED.getMessage());
        }

        // 2. Extract username from the token itself
        String username = jwtUtils.getUserNameFromToken(clientRefreshToken);
        log.info("Refresh token request for user: {}", username);

        // 3. Compare with the server-side stored token (refresh token rotation guard)
        String storedRefreshToken = tokenRedisService.getRefreshToken(username);
        if (storedRefreshToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    EnumAuthError.REFRESH_TOKEN_NOT_FOUND.getMessage());
        }
        if (!storedRefreshToken.equals(clientRefreshToken)) {
            // Token doesn't match stored copy — possible reuse/theft; revoke the session
            tokenRedisService.deleteRefreshToken(username);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    EnumAuthError.REFRESH_TOKEN_NOT_FOUND.getMessage());
        }

        // 4. Issue a fresh token pair
        Long userId = tokenRedisService.getUserId(username);
        String newAccessToken = jwtUtils.generateToken(username);
        String newRefreshToken = jwtUtils.generateRefreshToken(username);
        tokenRedisService.saveAccessToken(username, userId, newAccessToken);
        tokenRedisService.saveRefreshToken(username, userId, newRefreshToken);
        log.info("Tokens rotated for user: {}", username);

        return Map.of(
                "token", newAccessToken,
                "refreshToken", newRefreshToken);
    }

    // --- Logout ------------------------------------------------------------------

    @Override
    public void logout(String username) {
        tokenRedisService.deleteAccessToken(username);
        tokenRedisService.deleteRefreshToken(username);
        log.info("Logged out user: {}", username);
    }

    // --- Private helpers ---------------------------------------------------------

    /**
     * Issues a new access/refresh token pair, persists both in Redis and
     * returns them so the caller (controller) can set the cookie.
     */
    private Map<String, String> issueTokenPair(UserEntity user) {
        String accessToken = jwtUtils.generateToken(user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
        tokenRedisService.saveAccessToken(user.getUsername(), user.getId(), accessToken);
        tokenRedisService.saveRefreshToken(user.getUsername(), user.getId(), refreshToken);
        return Map.of(
                "token", accessToken,
                "refreshToken", refreshToken);
    }

    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1)
            return email;
        return email.charAt(0) + "***" + email.substring(at);
    }
}
