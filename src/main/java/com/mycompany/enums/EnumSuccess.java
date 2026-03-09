package com.mycompany.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum EnumSuccess {

    SUCCESS(200, "Success"),

    // Authentication Successes Enums
    LOGIN_SUCCESS(201, "Login successful"),
    REGISTRATION_SUCCESS(202, "Registration successful. Please verify your email."),
    EMAIL_VERIFIED(203, "Email verified successfully"),
    OTP_SENT(204, "OTP sent successfully"),
    PASSWORD_RESET_SUCCESS(205, "Password reset successfully"),

    // Course Successes Enums
    COURSE_UPDATE_SUCCESS(300, "Course updated successfully"),
    COURSE_DELETION_SUCCESS(301, "Course deleted successfully"),
    COURSE_CREATION_SUCCESS(302, "Course created successfully"),

    // Lesson Successes Enums
    LESSON_CREATION_SUCCESS(310, "Lesson created successfully"),
    LESSON_UPDATE_SUCCESS(311, "Lesson updated successfully"),
    LESSON_DELETION_SUCCESS(312, "Lesson deleted successfully"),
    LESSON_COMPLETION_SUCCESS(313, "Lesson completed successfully"),
    ;

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
