package com.mycompany.service;

import java.util.List;

import com.mycompany.dto.response.UserLessonResponse;

public interface LessonProgressService {

    String completeLesson(Long userId, Long lessonId);

    List<UserLessonResponse> getUserCompletedLessons(Long userId);

    List<UserLessonResponse> getUserActiveLessons(Long userId);
}