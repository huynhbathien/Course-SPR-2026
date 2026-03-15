package com.mycompany.service.Impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mycompany.dto.response.UserLessonResponse;
import com.mycompany.entity.Lesson;
import com.mycompany.entity.UserEntity;
import com.mycompany.entity.UserLesson;
import com.mycompany.enums.EnumAuthError;
import com.mycompany.enums.EnumError;
import com.mycompany.enums.EnumSuccess;
import com.mycompany.mapstruct.UserLessonMapper;
import com.mycompany.repository.LessonRepository;
import com.mycompany.repository.UserLessonRepository;
import com.mycompany.repository.UserRepository;
import com.mycompany.service.LessonProgressService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LessonProgressServiceImpl implements LessonProgressService {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final UserLessonRepository userLessonRepository;
    private final UserLessonMapper userLessonMapper;

    @Override
    @Transactional
    public String completeLesson(Long userId, Long lessonId) {
        UserEntity user = findUserOrThrow(userId);
        Lesson lesson = findLessonOrThrow(lessonId);

        if (!hasCourseAccess(user, lesson)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "User does not have access to course containing this lesson");
        }

        UserLesson userLesson = userLessonRepository.findByUserAndLesson(user, lesson)
                .orElseGet(() -> createUserLesson(user, lesson));

        userLesson.setCompleted(true);
        userLesson.setCompletedDate(LocalDateTime.now());
        userLessonRepository.save(userLesson);

        activateDependentLessons(user, lesson);
        return EnumSuccess.LESSON_COMPLETION_SUCCESS.getMessage();
    }

    @Override
    public List<UserLessonResponse> getUserCompletedLessons(Long userId) {
        UserEntity user = findUserOrThrow(userId);
        return userLessonMapper.toUserLessonResponseList(userLessonRepository.findByUserAndCompletedTrue(user));
    }

    @Override
    public List<UserLessonResponse> getUserActiveLessons(Long userId) {
        UserEntity user = findUserOrThrow(userId);
        return userLessonMapper.toUserLessonResponseList(userLessonRepository.findByUserAndActiveTrue(user));
    }

    private void activateDependentLessons(UserEntity user, Lesson completedLesson) {
        if (!userLessonRepository.existsByUserAndLessonAndCompletedTrue(user, completedLesson)) {
            return;
        }

        List<Lesson> dependentLessons = lessonRepository.findByLessonRequire(completedLesson.getId());
        for (Lesson dependentLesson : dependentLessons) {
            if (!hasCourseAccess(user, dependentLesson)) {
                continue;
            }

            UserLesson dependentUserLesson = userLessonRepository.findByUserAndLesson(user, dependentLesson)
                    .orElseGet(() -> createUserLesson(user, dependentLesson));
            dependentUserLesson.setActive(true);
            userLessonRepository.save(dependentUserLesson);
        }
    }

    private boolean hasCourseAccess(UserEntity user, Lesson lesson) {
        return user.getUserCourses().stream()
                .anyMatch(userCourse -> userCourse.getCourse().getId().equals(lesson.getCourse().getId())
                        && userCourse.isActive());
    }

    private UserEntity findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        EnumAuthError.USER_NOT_FOUND.getMessage() + " with id: " + userId));
    }

    private Lesson findLessonOrThrow(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        EnumError.LESSON_NOT_FOUND.getMessage() + " with id: " + lessonId));
    }

    private UserLesson createUserLesson(UserEntity user, Lesson lesson) {
        UserLesson userLesson = new UserLesson();
        userLesson.setUser(user);
        userLesson.setLesson(lesson);
        return userLesson;
    }
}