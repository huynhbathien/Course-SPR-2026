package com.mycompany.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CourseResponse {
    private String id;
    private String title;
    private String type;
    private String linkImg;
    private String description;
    private int totalLessons;
}
