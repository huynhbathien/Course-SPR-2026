package com.mycompany.service;

import java.util.HashMap;
import java.util.Map;

import com.mycompany.dto.request.LoginRequestDTO;
import com.mycompany.dto.request.RegisterRequestDTO;

public interface AuthService {
    HashMap<String, String> login(LoginRequestDTO authRequestDTO);

    HashMap<String, String> register(RegisterRequestDTO registerRequestDTO);

    Map<String, Object> refreshToken(String username);

    void logout(String username);
}
