package com.mycompany.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCourseResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseType;
    private boolean isActive;
    private LocalDateTime purchaseDate;
    private LocalDateTime completionDate;
    private int progress; // 0-100
}
