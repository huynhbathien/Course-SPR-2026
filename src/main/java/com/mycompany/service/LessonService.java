package com.mycompany.service;

import java.util.List;

import com.mycompany.dto.request.LessonRequest;
import com.mycompany.dto.response.LessonResponse;
import com.mycompany.dto.response.UserLessonResponse;

public interface LessonService {

    LessonResponse createLesson(LessonRequest lessonRequest);

    LessonResponse getLessonDetails(Long lessonId);

    LessonResponse updateLesson(Long lessonId, LessonRequest lessonRequest);

    String deleteLesson(Long lessonId);

    List<LessonResponse> getLessonsByCourse(Long courseId);

    /**
     * Đánh dấu lesson là đã hoàn thành bởi user
     * Nếu lesson có dependent lessons, sẽ activate chúng
     */
    String completeLesson(Long userId, Long lessonId);

    /**
     * Lấy danh sách lesson đã complete của user
     */
    List<UserLessonResponse> getUserCompletedLessons(Long userId);

    /**
     * Lấy danh sách lesson active của user
     */
    List<UserLessonResponse> getUserActiveLessons(Long userId);

    List<LessonResponse> searchLessons(String keyword);

}
