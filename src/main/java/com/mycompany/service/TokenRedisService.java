package com.mycompany.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.mycompany.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Token Redis Service - Quản lý Access Token & Refresh Token trong Redis
 * 
 * Refresh Token: 7-30 days, lưu server-side để có thể revoke
 * Access Token: 15-30 minutes, lưu ở client (FE), không cần lưu server
 * (optional)
 */
@Slf4j
@Service
public class TokenRedisService {

    @Autowired
    private RedisUtil redisUtil;

    @Value("${token.access-token-expiration:1800}")
    private long accessTokenExpiration; // in seconds (15-30 minutes)

    @Value("${token.refresh-token-expiration:604800}")
    private long refreshTokenExpiration; // in seconds (7 days)

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String USER_SESSION_PREFIX = "user_session:";

    // ==================== REFRESH TOKEN OPERATIONS ====================

    /**
     * Lưu refresh token vào Redis (kèm userId)
     * 
     * @param username     username/email
     * @param userId       user ID
     * @param refreshToken refresh token value
     */
    public void saveRefreshToken(String username, Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + username;
        try {
            // Lưu refresh token với TTL
            redisUtil.set(key, refreshToken, refreshTokenExpiration, TimeUnit.SECONDS);

            // Lưu kèm userId để track
            String userKey = USER_SESSION_PREFIX + username;
            redisUtil.hset(userKey, "userId", String.valueOf(userId));
            redisUtil.hset(userKey, "refreshToken", refreshToken);
            redisUtil.hset(userKey, "loginTime", String.valueOf(System.currentTimeMillis()));
            redisUtil.expire(userKey, refreshTokenExpiration, TimeUnit.SECONDS);

            log.info("Refresh token saved for user: {} (userId: {})", username, userId);
        } catch (Exception e) {
            log.error("Error saving refresh token for user: {}", username, e);
            throw e;
        }
    }

    /**
     * Lấy refresh token từ Redis
     * 
     * @param username username/email
     * @return refresh token hoặc null
     */
    public String getRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        try {
            String token = redisUtil.get(key);
            if (token != null) {
                log.debug("Refresh token retrieved for user: {}", username);
            }
            return token;
        } catch (Exception e) {
            log.error("Error retrieving refresh token for user: {}", username, e);
            return null;
        }
    }

    /**
     * Validate refresh token (check expire)
     * 
     * @param username username/email
     * @return true nếu token còn hạn
     */
    public boolean validateRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        try {
            return redisUtil.exists(key);
        } catch (Exception e) {
            log.error("Error validating refresh token for user: {}", username, e);
            return false;
        }
    }

    /**
     * Xóa refresh token (logout)
     * 
     * @param username username/email
     */
    public void deleteRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        String userKey = USER_SESSION_PREFIX + username;
        try {
            redisUtil.delete(key);
            redisUtil.delete(userKey);
            log.info("Refresh token deleted for user: {}", username);
        } catch (Exception e) {
            log.error("Error deleting refresh token for user: {}", username, e);
        }
    }

    /**
     * Lấy TTL của refresh token (còn bao lâu hết hạn)
     * 
     * @param username username/email
     * @return số giây còn lại
     */
    public long getRefreshTokenExpire(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        try {
            return redisUtil.getExpire(key);
        } catch (Exception e) {
            log.error("Error getting refresh token expiry for user: {}", username, e);
            return -2; // key not found
        }
    }

    // ==================== ACCESS TOKEN OPERATIONS (Optional - lưu ở client)
    // ====================

    /**
     * Lưu access token vào Redis (optional - cho việc tracking)
     * Thường FE sẽ lưu access token, server không cần lưu
     * 
     * @param username    username/email
     * @param userId      user ID
     * @param accessToken access token value
     */
    public void saveAccessToken(String username, Long userId, String accessToken) {
        String key = ACCESS_TOKEN_PREFIX + username;
        try {
            redisUtil.set(key, accessToken, accessTokenExpiration, TimeUnit.SECONDS);
            log.debug("Access token saved for user: {} (userId: {})", username, userId);
        } catch (Exception e) {
            log.error("Error saving access token for user: {}", username, e);
        }
    }

    /**
     * Lấy access token từ Redis
     * 
     * @param username username/email
     * @return access token hoặc null
     */
    public String getAccessToken(String username) {
        String key = ACCESS_TOKEN_PREFIX + username;
        try {
            return redisUtil.get(key);
        } catch (Exception e) {
            log.error("Error retrieving access token for user: {}", username, e);
            return null;
        }
    }

    /**
     * Xóa access token
     * 
     * @param username username/email
     */
    public void deleteAccessToken(String username) {
        String key = ACCESS_TOKEN_PREFIX + username;
        try {
            redisUtil.delete(key);
            log.debug("Access token deleted for user: {}", username);
        } catch (Exception e) {
            log.error("Error deleting access token for user: {}", username, e);
        }
    }

    // ==================== USER SESSION OPERATIONS ====================

    /**
     * Lấy userId từ user session
     * 
     * @param username username/email
     * @return userId hoặc null
     */
    public Long getUserId(String username) {
        String userKey = USER_SESSION_PREFIX + username;
        try {
            Object userIdObj = redisUtil.hget(userKey, "userId");
            if (userIdObj != null) {
                return Long.parseLong(userIdObj.toString());
            }
        } catch (Exception e) {
            log.error("Error getting userId for user: {}", username, e);
        }
        return null;
    }

    /**
     * Lấy thông tin user session
     * 
     * @param username username/email
     * @return userId
     */
    public String getUserSessionInfo(String username) {
        String userKey = USER_SESSION_PREFIX + username;
        try {
            Object userIdObj = redisUtil.hget(userKey, "userId");
            Object loginTimeObj = redisUtil.hget(userKey, "loginTime");

            if (userIdObj != null) {
                long loginTime = loginTimeObj != null ? Long.parseLong(loginTimeObj.toString()) : 0;
                long now = System.currentTimeMillis();
                long sessionDuration = (now - loginTime) / 1000; // in seconds

                return String.format("userId=%s, sessionDuration=%ds", userIdObj, sessionDuration);
            }
        } catch (Exception e) {
            log.error("Error getting user session info for user: {}", username, e);
        }
        return null;
    }

    /**
     * Xóa toàn bộ user session
     * 
     * @param username username/email
     */
    public void deleteUserSession(String username) {
        String userKey = USER_SESSION_PREFIX + username;
        try {
            redisUtil.delete(userKey);
            log.debug("User session deleted for user: {}", username);
        } catch (Exception e) {
            log.error("Error deleting user session for user: {}", username, e);
        }
    }

    // ==================== BLACKLIST OPERATIONS (For Token Revocation)
    // ====================
    private static final String BLACKLIST_PREFIX = "token_blacklist:";

    /**
     * Thêm token vào blacklist (revoke token)
     * Dùng khi user logout hoặc change password
     * 
     * @param token      token value
     * @param expiryTime token expiration time (ms)
     */
    public void addToBlacklist(String token, long expiryTime) {
        String key = BLACKLIST_PREFIX + token;
        try {
            long now = System.currentTimeMillis();
            long ttlSeconds = (expiryTime - now) / 1000;

            if (ttlSeconds > 0) {
                redisUtil.set(key, "revoked", ttlSeconds, TimeUnit.SECONDS);
                log.info("Token added to blacklist with TTL: {} seconds", ttlSeconds);
            }
        } catch (Exception e) {
            log.error("Error adding token to blacklist", e);
        }
    }

    /**
     * Kiểm tra token có trong blacklist không
     * 
     * @param token token value
     * @return true nếu token bị revoke
     */
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        try {
            return redisUtil.exists(key);
        } catch (Exception e) {
            log.error("Error checking token blacklist", e);
            return false;
        }
    }

    /**
     * Xóa token khỏi blacklist
     * 
     * @param token token value
     */
    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        try {
            redisUtil.delete(key);
            log.debug("Token removed from blacklist");
        } catch (Exception e) {
            log.error("Error removing token from blacklist", e);
        }
    }
}
