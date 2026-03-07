package com.mycompany.config;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.dto.APIResponse;
import com.mycompany.enums.EnumAuthError;
import com.mycompany.exception.TokenExpiredException;
import com.mycompany.exception.TokenRevokedException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(0)
public class JwtExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            log.error("JWT Exception: {}", ex.getMessage());
            handleJwtException(response, ex);
        }
    }

    private void handleJwtException(HttpServletResponse response, Exception ex) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Differentiate between different JWT error types
        EnumAuthError errorType = EnumAuthError.TOKEN_INVALID;

        if (ex instanceof TokenExpiredException) {
            errorType = EnumAuthError.TOKEN_EXPIRED;
            log.warn("Token expired: {}", ex.getMessage());
        } else if (ex instanceof TokenRevokedException) {
            errorType = EnumAuthError.TOKEN_REVOKED;
            log.warn("Token revoked/blacklisted: {}", ex.getMessage());
        } else if (ex.getMessage() != null && ex.getMessage().contains("expired")) {
            errorType = EnumAuthError.TOKEN_EXPIRED;
            log.warn("Token expired (detected from message): {}", ex.getMessage());
        } else if (ex.getMessage() != null && ex.getMessage().contains("revoked")) {
            errorType = EnumAuthError.TOKEN_REVOKED;
            log.warn("Token revoked (detected from message): {}", ex.getMessage());
        } else {
            log.error("Invalid token: {}", ex.getMessage());
        }

        final APIResponse<Object> body = APIResponse.error(
                errorType.getCode(),
                errorType.getMessage(),
                ex.getMessage());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
