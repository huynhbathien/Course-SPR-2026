package com.mycompany.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum EnumAuthError {
    // Authentication errors
    INVALID_CREDENTIALS(1001, "Invalid username or password"),
    ACCOUNT_DISABLED(1002, "Account is disabled"),
    ACCOUNT_LOCKED(1003, "Account is locked"),
    ACCOUNT_EXPIRED(1004, "Account has expired"),

    // Token errors
    TOKEN_EXPIRED(2001, "Token has expired"),
    TOKEN_INVALID(2002, "Invalid token"),
    TOKEN_MISSING(2003, "Token is missing"),
    TOKEN_MALFORMED(2004, "Malformed token"),

    // Authorization errors
    INSUFFICIENT_PERMISSIONS(3001, "Insufficient permissions"),
    ACCESS_DENIED(3002, "Access denied"),
    ROLE_NOT_FOUND(3003, "Role not found"),

    // User errors
    USER_NOT_FOUND(4001, "User not found"),
    USER_ALREADY_EXISTS(4002, "User already exists"),
    EMAIL_ALREADY_EXISTS(4003, "Email already exists"),
    USERNAME_ALREADY_EXISTS(4004, "Username already exists"),

    // Password errors
    PASSWORD_WEAK(5001, "Password is too weak"),
    PASSWORD_MISMATCH(5002, "Passwords do not match"),
    INVALID_OLD_PASSWORD(5003, "Invalid old password"),

    // Session errors
    SESSION_EXPIRED(6001, "Session has expired"),
    SESSION_INVALID(6002, "Invalid session"),
    MAX_SESSIONS_EXCEEDED(6003, "Maximum number of sessions exceeded"),

    // General errors
    UNAUTHORIZED(9001, "Unauthorized access"),
    FORBIDDEN(9002, "Forbidden"),
    INTERNAL_ERROR(9999, "Internal server error");

    int code;
    String message;

    EnumAuthError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static EnumAuthError fromCode(int code) {
        for (EnumAuthError error : EnumAuthError.values()) {
            if (error.code == code) {
                return error;
            }
        }
        throw new IllegalArgumentException("Invalid error code: " + code);
    }
}
