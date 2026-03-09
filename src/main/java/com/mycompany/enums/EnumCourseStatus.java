package com.mycompany.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum EnumCourseStatus {

    DRAFT("Draft", "Course is being drafted"),
    PUBLISHED("Published", "Course is published and publicly visible");

    String displayName;
    String description;

    EnumCourseStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
