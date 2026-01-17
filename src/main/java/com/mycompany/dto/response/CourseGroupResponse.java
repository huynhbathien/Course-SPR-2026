package com.mycompany.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseGroupResponse {

    private String courseTypeCode;
    private List<CourseResponse> courses;

}
