package com.mycompany.service;

import java.util.List;

import com.mycompany.dto.request.CourseRequest;
import com.mycompany.dto.response.CourseGroupResponse;
import com.mycompany.dto.response.CourseResponse;

public interface CourseService {

    public CourseResponse getCourseDetails(Long courseId);

    public CourseResponse createCourse(CourseRequest courseData);

    public CourseResponse updateCourse(Long courseId, CourseRequest courseData);

    public String deleteCourse(Long courseId);

    public List<CourseGroupResponse> listAllCourses();

    /**
     * User mua khóa học - activate course
     */
    public CourseResponse purchaseCourse(Long userId, Long courseId);
}
