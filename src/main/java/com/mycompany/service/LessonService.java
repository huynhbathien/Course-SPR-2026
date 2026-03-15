package com.mycompany.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mycompany.dto.request.LessonRequest;
import com.mycompany.dto.response.LessonResponse;

public interface LessonService {

    LessonResponse createLesson(LessonRequest lessonRequest);

    LessonResponse getLessonDetails(Long lessonId);

    LessonResponse updateLesson(Long lessonId, LessonRequest lessonRequest);

    String deleteLesson(Long lessonId);

    List<LessonResponse> getLessonsByCourse(Long courseId);

    Page<LessonResponse> searchLessons(String keyword, Pageable pageable);

}
