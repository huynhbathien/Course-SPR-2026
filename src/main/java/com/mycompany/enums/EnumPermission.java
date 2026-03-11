package com.mycompany.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum EnumPermission {
    // Course permissions
    COURSE_READ("course:read", "Read course information"),
    COURSE_WRITE("course:write", "Create and edit courses"),
    COURSE_DELETE("course:delete", "Delete courses"),

    // Lesson permissions
    LESSON_READ("lesson:read", "Read lesson information"),
    LESSON_WRITE("lesson:write", "Create and edit lessons"),
    LESSON_DELETE("lesson:delete", "Delete lessons"),

    // User permissions
    USER_READ("user:read", "Read user information"),
    USER_WRITE("user:write", "Create and edit users"),
    USER_DELETE("user:delete", "Delete users"),

    // Enrollment permissions
    ENROLLMENT_MANAGE("enrollment:manage", "Manage course enrollments"),

    // Admin permissions
    ADMIN_ACCESS("admin:access", "Access admin panel"),
    SYSTEM_CONFIG("system:config", "Configure system settings");

    String permission;
    String description;

    EnumPermission(String permission, String description) {
        this.permission = permission;
        this.description = description;
    }

    public static EnumPermission fromPermission(String permission) {
        for (EnumPermission perm : EnumPermission.values()) {
            if (perm.permission.equalsIgnoreCase(permission)) {
                return perm;
            }
        }
        throw new IllegalArgumentException("Invalid permission: " + permission);
    }
}
