package com.mycompany.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponse {
    private Long id;
    private String title;
    private String content;
    private Long courseId;
    private Long lessonRequireId; // ID of prerequisite lesson
}
