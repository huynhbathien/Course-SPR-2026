package com.mycompany.dto.response;

import java.time.LocalDateTime;

import com.mycompany.enums.EnumCourseStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCourseResponse {

    private Long id;
    private String title;
    private String type;
    private String linkImg;
    private String description;
    private int totalLessons;
    private EnumCourseStatus status;
    private long enrollmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
