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
import com.mycompany.dto.request.LessonRequest;
import com.mycompany.dto.response.LessonResponse;
import com.mycompany.dto.response.UserLessonResponse;
import com.mycompany.enums.EnumSuccess;
import com.mycompany.service.LessonService;

import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/lesson")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class LessonController {

    final LessonService lessonService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public APIResponse<LessonResponse> createLesson(@Valid @RequestBody LessonRequest lessonRequest) {
        LessonResponse data = lessonService.createLesson(lessonRequest);
        return APIResponse.success(EnumSuccess.LESSON_CREATION_SUCCESS.getCode(),
                EnumSuccess.LESSON_CREATION_SUCCESS.getMessage(), data);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{lessonId}")
    public APIResponse<LessonResponse> getLessonDetail(@PathVariable Long lessonId) {
        LessonResponse data = lessonService.getLessonDetails(lessonId);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), EnumSuccess.SUCCESS.getMessage(), data);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{lessonId}")
    public APIResponse<LessonResponse> updateLesson(@PathVariable Long lessonId,
            @Valid @RequestBody LessonRequest lessonRequest) {
        LessonResponse data = lessonService.updateLesson(lessonId, lessonRequest);
        return APIResponse.success(EnumSuccess.LESSON_UPDATE_SUCCESS.getCode(),
                EnumSuccess.LESSON_UPDATE_SUCCESS.getMessage(), data);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{lessonId}")
    public APIResponse<String> deleteLesson(@PathVariable Long lessonId) {
        String data = lessonService.deleteLesson(lessonId);
        return APIResponse.success(EnumSuccess.LESSON_DELETION_SUCCESS.getCode(), data, data);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/course/{courseId}")
    public APIResponse<List<LessonResponse>> getLessonsByCourse(@PathVariable Long courseId) {
        List<LessonResponse> data = lessonService.getLessonsByCourse(courseId);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), EnumSuccess.SUCCESS.getMessage(), data);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{userId}/lesson/{lessonId}/complete")
    public APIResponse<String> completeLesson(@PathVariable Long userId, @PathVariable Long lessonId) {
        String data = lessonService.completeLesson(userId, lessonId);
        return APIResponse.success(EnumSuccess.LESSON_COMPLETION_SUCCESS.getCode(),
                EnumSuccess.LESSON_COMPLETION_SUCCESS.getMessage(), data);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/{userId}/completed")
    public APIResponse<List<UserLessonResponse>> getUserCompletedLessons(@PathVariable Long userId) {
        List<UserLessonResponse> data = lessonService.getUserCompletedLessons(userId);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), EnumSuccess.SUCCESS.getMessage(), data);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/{userId}/active")
    public APIResponse<List<UserLessonResponse>> getUserActiveLessons(@PathVariable Long userId) {
        List<UserLessonResponse> data = lessonService.getUserActiveLessons(userId);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), EnumSuccess.SUCCESS.getMessage(), data);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    public APIResponse<List<LessonResponse>> searchLessons(@RequestParam String keyword) {
        List<LessonResponse> data = lessonService.searchLessons(keyword);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), EnumSuccess.SUCCESS.getMessage(), data);
    }
}
