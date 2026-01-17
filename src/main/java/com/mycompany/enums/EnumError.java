package com.mycompany.enums;

import lombok.Getter;

@Getter
public enum EnumError {

    // Course Error Enums
    COURSE_NOT_FOUND(311, "Course not found"),
    COURSE_ALREADY_EXISTS(312, "Course already exists"),
    INVALID_COURSE_DATA(313, "Invalid course data provided");
    ;

    private int code;
    private String message;

    EnumError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static EnumError fromCode(int code) {
        for (EnumError error : EnumError.values()) {
            if (error.code == code) {
                return error;
            }
        }
        throw new IllegalArgumentException("Invalid course error code: " + code);

    }
}
