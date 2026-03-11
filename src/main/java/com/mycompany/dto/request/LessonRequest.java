package com.mycompany.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonRequest {

    @NotBlank(message = "Title is mandatory")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Content is mandatory")
    @Size(min = 10, max = 50000, message = "Content must be between 10 and 50000 characters")
    private String content;

    @NotNull(message = "Course ID is mandatory")
    private Long courseId;

    private Long lessonRequireId;
}
