package com.mycompany.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class JwtUtils {

    @Value("${jwt.secret-file}")
    private String jwtSecretFile;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpirationMs;

    private SecretKey secretKey;

    public SecretKey readJwtSecret() {
        if (secretKey != null) {
            return secretKey;
        }
        String secretToUse = "default";
        if (jwtSecretFile != null && !jwtSecretFile.isEmpty()) {
            try {
                String fileSecret = Files.readString(Paths.get(jwtSecretFile)).trim();
                if (!fileSecret.isEmpty()) {
                    secretToUse = fileSecret;
                }
            } catch (IOException e) {
                log.error("Error reading JWT secret from file: {}", e.getMessage());
                throw new RuntimeException("Could not read JWT secret from file", e);
            }
            secretKey = Keys.hmacShaKeyFor(secretToUse.getBytes());
            log.debug("SecretKey initialized successfully from file,length={}", secretToUse.length());
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