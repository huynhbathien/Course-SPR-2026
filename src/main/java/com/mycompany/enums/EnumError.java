package com.mycompany.enums;

import lombok.Getter;

@Getter
public enum EnumError {

    // Course Error Enums
    COURSE_NOT_FOUND(311, "Course not found"),
    COURSE_ALREADY_EXISTS(312, "Course already exists"),
    INVALID_COURSE_DATA(313, "Invalid course data provided"),

    // Lesson Error Enums
    LESSON_NOT_FOUND(321, "Lesson not found"),
    LESSON_ALREADY_EXISTS(322, "Lesson already exists with title"),
    LESSON_REQUIRE_NOT_FOUND(323, "Lesson require not found"),

    // Admin Error Enums
    USER_NOT_FOUND(401, "User not found"),
    INVALID_ROLE(402, "Invalid role specified"),
    COURSE_ALREADY_PUBLISHED(411, "Course is already published"),
    COURSE_ALREADY_DRAFT(412, "Course is already in draft");

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
