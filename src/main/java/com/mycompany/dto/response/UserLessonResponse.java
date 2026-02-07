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
public class UserLessonResponse {
    private Long id;
    private Long lessonId;
    private String lessonTitle;
    private boolean isActive;
    private boolean isCompleted;
    private LocalDateTime completedDate;
    private Integer score;
}
