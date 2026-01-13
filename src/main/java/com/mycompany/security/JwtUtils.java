package com.mycompany.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class JwtUtils {

    @Value("${jwt.secret-file}")
    String jwtSecretFile;

    @Value("${jwt.expiration}")
    long jwtExpirationMs;

    @Value("${jwt.refresh-expiration}")
    long jwtRefreshExpirationMs;

    SecretKey secretKey;

    @PostConstruct
    public void initializeSecretKey() {
        readJwtSecret();
    }

    public SecretKey readJwtSecret() {
        if (secretKey != null) {
            return secretKey;
        }
        String secretToUse = null;

        if (jwtSecretFile != null && !jwtSecretFile.isEmpty()) {
            try {
                String fileSecret = Files.readString(Paths.get(jwtSecretFile)).trim();
                if (!fileSecret.isEmpty()) {
                    secretToUse = fileSecret;
                }
            } catch (IOException e) {
                log.warn("JWT secret file not found: {}", jwtSecretFile);
            }
        }

        // Decode base64 secret and create key
        if (secretToUse != null && !secretToUse.isEmpty()) {
            try {
                byte[] decodedKey = Base64.getDecoder().decode(secretToUse);
                secretKey = Keys.hmacShaKeyFor(decodedKey);
                log.info("SecretKey initialized successfully from file, key size={} bits", decodedKey.length * 8);
            } catch (IllegalArgumentException e) {
                log.error("Invalid base64 format for JWT secret: {}", e.getMessage());
                throw new RuntimeException("Invalid JWT secret format", e);
            }
        } else {
            // Generate a new secure key if no secret file provided
            secretKey = Jwts.SIG.HS512.key().build();
            log.warn("Generated new secure JWT key (HS512)");
        }

        return secretKey;
    }

    public String generateToken(String userID) {
        return generateToken(userID, null);
    }

    public String generateToken(String userName, String email) {
        var token = Jwts.builder()
                .subject(userName)
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .issuedAt(new Date())
                .signWith(readJwtSecret());
        if (email != null) {
            token.claim("email", email);
        }
        return token.compact();
    }

    public String getUserNameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            Claims claim = getClaimsFromToken(token);
            boolean isValid = userDetails.getUsername().equals(claim.getSubject()) && !isTokenExpired(token);

            boolean isActive = true;
            if (userDetails instanceof CustomUserDetailsService.CustomUserDetails customUserDetails) {
                isActive = customUserDetails.getUser().isActive();
            }

            return isValid && isActive;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public Boolean isTokenExpired(String token) {
        try {
            Claims claim = getClaimsFromToken(token);
            Date expiration = claim.getExpiration();
            if (expiration.before(new Date()))
                return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }
}