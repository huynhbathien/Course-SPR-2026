package com.mycompany.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.dto.APIResponse;
import com.mycompany.dto.request.CourseRequest;
import com.mycompany.dto.response.CourseGroupResponse;
import com.mycompany.dto.response.CourseResponse;
import com.mycompany.enums.EnumSuccess;
import com.mycompany.service.CourseService;

import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/course")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CourseController {

    final CourseService courseService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{courseId}")
    public APIResponse<CourseResponse> getCourseDetail(@PathVariable Long courseId) {
        CourseResponse data = courseService.getCourseDetails(courseId);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), EnumSuccess.SUCCESS.getMessage(), data);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public APIResponse<CourseResponse> createCourse(@Valid @RequestBody CourseRequest courseRequest) {
        CourseResponse data = courseService.createCourse(courseRequest);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), EnumSuccess.SUCCESS.getMessage(), data);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{courseId}")
    public APIResponse<CourseResponse> updateCourse(@PathVariable Long courseId,
            @Valid @RequestBody CourseRequest courseRequest) {
        CourseResponse data = courseService.updateCourse(courseId, courseRequest);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), EnumSuccess.SUCCESS.getMessage(), data);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{courseId}")
    public APIResponse<String> deleteCourse(@PathVariable Long courseId) {
        String message = courseService.deleteCourse(courseId);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), EnumSuccess.SUCCESS.getMessage(), message);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public APIResponse<List<CourseGroupResponse>> listAllCourses() {
        List<CourseGroupResponse> data = courseService.listAllCourses();
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), EnumSuccess.SUCCESS.getMessage(), data);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    public APIResponse<List<CourseResponse>> searchCourses(@RequestParam String keyword) {
        List<CourseResponse> data = courseService.searchCourses(keyword);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), EnumSuccess.SUCCESS.getMessage(), data);
    }

}
