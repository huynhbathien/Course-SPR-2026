package com.mycompany.service.Impl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mycompany.dto.request.ForgotPasswordRequestDTO;
import com.mycompany.dto.request.LoginRequestDTO;
import com.mycompany.dto.request.RegisterRequestDTO;
import com.mycompany.dto.request.ResendOtpRequestDTO;
import com.mycompany.dto.request.ResetPasswordRequestDTO;
import com.mycompany.dto.request.VerifyOtpRequestDTO;
import com.mycompany.service.AuthAccountService;
import com.mycompany.service.AuthService;
import com.mycompany.service.AuthSessionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

    AuthSessionService authSessionService;
    AuthAccountService authAccountService;

    @Override
    public Map<String, String> login(@Valid LoginRequestDTO dto, String clientIp) {
        return authSessionService.login(dto, clientIp);
    }

    @Override
    public Map<String, String> register(@Valid RegisterRequestDTO dto) {
        return authAccountService.register(dto);
    }

    @Override
    public Map<String, String> verifyEmailAndLogin(@Valid VerifyOtpRequestDTO dto) {
        return authAccountService.verifyEmailAndLogin(dto);
    }

    @Override
    public void resendOtp(@Valid ResendOtpRequestDTO dto) {
        authAccountService.resendOtp(dto);
    }

    @Override
    public void forgotPassword(@Valid ForgotPasswordRequestDTO dto) {
        authAccountService.forgotPassword(dto);
    }

    @Override
    public void resetPassword(@Valid ResetPasswordRequestDTO dto) {
        authAccountService.resetPassword(dto);
    }

    @Override
    public Map<String, String> refreshToken(String clientRefreshToken) {
        return authSessionService.refreshToken(clientRefreshToken);
    }

    @Override
    public void logout(String username) {
        authSessionService.logout(username);
    }
}
