package com.mycompany.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class CourseResponse {
    private Long id;
    private String title;
    private String type;
    private String linkImg;
    private String description;
    private int totalLessons;
}
