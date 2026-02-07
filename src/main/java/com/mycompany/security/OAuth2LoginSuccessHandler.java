package com.mycompany.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.service.TokenRedisService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;
    private final TokenRedisService tokenRedisService;

    public OAuth2LoginSuccessHandler(OAuth2AuthorizedClientService authorizedClientService, JwtUtils jwtUtils,
            TokenRedisService tokenRedisService) {
        this.authorizedClientService = authorizedClientService;
        this.jwtUtils = jwtUtils;
        this.objectMapper = new ObjectMapper();
        this.tokenRedisService = tokenRedisService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        try {
            OAuth2AuthenticationToken oAuth2Token = (OAuth2AuthenticationToken) authentication;
            String registrationId = oAuth2Token.getAuthorizedClientRegistrationId();
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String username = getPrincipalName(oAuth2User);

            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Unable to extract username from OAuth2 principal");
            }

            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(registrationId,
                    username);

            UserDetails userDetails = jwtUtils.processOAuth2User(registrationId, oAuth2User, authorizedClient);
            String jwt = jwtUtils.generateToken(userDetails.getUsername());
            String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());

            log.info("OAuth2 login successful for user: {} via provider: {}", userDetails.getUsername(),
                    registrationId);

            // Get userId from userDetails
            Long userId = null;
            if (userDetails instanceof com.mycompany.security.CustomUserDetailsService.CustomUserDetails) {
                userId = ((com.mycompany.security.CustomUserDetailsService.CustomUserDetails) userDetails).getUserId();
            }

            // Lưu refresh token vào Redis (server-side) - không gửi cho client
            tokenRedisService.saveRefreshToken(userDetails.getUsername(), userId, refreshToken);
            log.debug("Refresh token saved to Redis for user: {} (userId: {})", userDetails.getUsername(), userId);

            // Use ObjectMapper for safe JSON serialization
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("token", jwt);
            responseBody.put("username", userDetails.getUsername());
            responseBody.put("userId", userId);
            responseBody.put("provider", registrationId);
            responseBody.put("message", "OAuth2 login successful");

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(responseBody));
            response.getWriter().flush();
            response.getWriter().close();
        } catch (Exception e) {
            log.error("OAuth2 authentication failed: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            try {
                Map<String, Object> errorBody = new HashMap<>();
                errorBody.put("error", "Authentication failed");
                errorBody.put("message", e.getMessage());
                response.getWriter().write(objectMapper.writeValueAsString(errorBody));
                response.getWriter().flush();
                response.getWriter().close();
            } catch (IOException ioException) {
                log.error("Failed to write error response", ioException);
            }
        }
    }

    public String getPrincipalName(OAuth2User oAuth2User) {
        // Try to get name first
        String name = oAuth2User.getAttribute("name");
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }

        // Try email
        String email = oAuth2User.getAttribute("email");
        if (email != null && !email.trim().isEmpty()) {
            return email.trim();
        }

        // Try login (for GitHub)
        String login = oAuth2User.getAttribute("login");
        if (login != null && !login.trim().isEmpty()) {
            return login.trim();
        }

        // Last resort: use login as ID
        Object id = oAuth2User.getAttribute("id");
        if (id != null) {
            return "user_" + id;
        }

        return null;
    }

}
