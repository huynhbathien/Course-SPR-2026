package com.mycompany.controller;

import com.mycompany.dto.request.APIResponse;
import com.mycompany.dto.request.LoginRequestDTO;
import com.mycompany.dto.request.RegisterRequestDTO;
import com.mycompany.enums.EnumSuccess;
import com.mycompany.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthController {

    AuthService authService;

    @PostMapping("/login")
    public APIResponse<Object> login(@RequestBody LoginRequestDTO authRequestDTO) {
        String token = authService.login(authRequestDTO);
        return APIResponse.builder()
                .statusCode(EnumSuccess.LOGIN_SUCCESS.getCode())
                .message(EnumSuccess.LOGIN_SUCCESS.getMessage())
                .data(token)
                .build();
    }

    @PostMapping("/register")
    public APIResponse<Object> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        String token = authService.register(registerRequestDTO);
        return APIResponse.builder()
                .statusCode(EnumSuccess.REGISTRATION_SUCCESS.getCode())
                .message(EnumSuccess.REGISTRATION_SUCCESS.getMessage())
                .data(token)
                .build();
    }

}
