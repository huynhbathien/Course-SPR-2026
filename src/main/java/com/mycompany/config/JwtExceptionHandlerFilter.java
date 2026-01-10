package com.mycompany.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.dto.request.APIResponse;
import com.mycompany.enums.EnumAuthError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.error("JWT Exception: {}", ex.getMessage());
            handleJwtException(response, ex);
        }
    }

    private void handleJwtException(HttpServletResponse response, Exception ex) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final APIResponse<Object> body = APIResponse.error(
                EnumAuthError.TOKEN_INVALID.getCode(),
                EnumAuthError.TOKEN_INVALID.getMessage(),
                ex.getMessage());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
