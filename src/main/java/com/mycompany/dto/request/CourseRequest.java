package com.mycompany.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {

    @NotBlank(message = "Title is mandatory")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Type is mandatory")
    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type;

    @NotBlank(message = "LinkImg is mandatory")
    @Size(max = 500, message = "LinkImg must not exceed 500 characters")
    private String linkImg;

    @NotBlank(message = "Description is mandatory")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

}
