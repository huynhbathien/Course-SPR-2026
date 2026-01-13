package com.mycompany.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.dto.request.APIResponse;
import com.mycompany.dto.request.LoginRequestDTO;
import com.mycompany.dto.request.RegisterRequestDTO;
import com.mycompany.enums.EnumSuccess;
import com.mycompany.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthController {

    AuthService authService;

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

}
