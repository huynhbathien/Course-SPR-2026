package com.mycompany.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum EnumSuccess {
    SUCCESS(200, "success"),
    LOGIN_SUCCESS(201, "Login successful"),
    REGISTRATION_SUCCESS(202, "Registration successful");

    String message;
    int code;

    EnumSuccess(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static EnumSuccess fromCode(int code) {
        for (EnumSuccess success : EnumSuccess.values()) {
            if (success.code == code) {
                return success;
            }
        }
        throw new IllegalArgumentException("Invalid success code: " + code);
    }

}
