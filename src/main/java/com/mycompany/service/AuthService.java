package com.mycompany.service;

import com.mycompany.dto.request.LoginRequestDTO;
import com.mycompany.dto.request.RegisterRequestDTO;

public interface AuthService {
    String login(LoginRequestDTO authRequestDTO);

    String register(RegisterRequestDTO registerRequestDTO);
}
