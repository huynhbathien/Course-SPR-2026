package com.mycompany.dto.request;

import jakarta.validation.constraints.NotBlank;
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
    private String title;

    @NotBlank(message = "Type is mandatory")
    private String type;

    @NotBlank(message = "LinkImg is mandatory")
    private String linkImg;

    @NotBlank(message = "Description is mandatory")
    private String description;

}
