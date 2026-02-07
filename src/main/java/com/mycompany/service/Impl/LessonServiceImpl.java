package com.mycompany.service.Impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mycompany.dto.request.LessonRequest;
import com.mycompany.dto.response.LessonResponse;
import com.mycompany.dto.response.UserLessonResponse;
import com.mycompany.entity.Course;
import com.mycompany.entity.Lesson;
import com.mycompany.entity.UserEntity;
import com.mycompany.entity.UserLesson;
import com.mycompany.enums.EnumAuthError;
import com.mycompany.enums.EnumError;
import com.mycompany.enums.EnumSuccess;
import com.mycompany.mapstruct.LessonMapper;
import com.mycompany.mapstruct.UserLessonMapper;
import com.mycompany.repository.CourseRepository;
import com.mycompany.repository.LessonRepository;
import com.mycompany.repository.UserLessonRepository;
import com.mycompany.repository.UserRepository;
import com.mycompany.service.LessonService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

        private final LessonRepository lessonRepository;
        private final CourseRepository courseRepository;
        private final UserRepository userRepository;
        private final UserLessonRepository userLessonRepository;
        private final LessonMapper lessonMapper;
        private final UserLessonMapper userLessonMapper;

        @Transactional
        @Override
        public LessonResponse createLesson(@Valid LessonRequest lessonRequest) {
                Course course = courseRepository.findById(lessonRequest.getCourseId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                EnumError.COURSE_NOT_FOUND.getMessage() + " with id: "
                                                                + lessonRequest.getCourseId()));

                if (lessonRepository.findByTitle(lessonRequest.getTitle()).isPresent()) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                        EnumError.LESSON_ALREADY_EXISTS.getMessage() + " with title: "
                                                        + lessonRequest.getTitle());
                }

                Lesson lesson = lessonMapper.toLessonEntity(lessonRequest);
                lesson.setCourse(course);

                // Set lessonRequire nếu có lessonRequireId
                if (lessonRequest.getLessonRequireId() != null) {
                        Lesson lessonRequire = lessonRepository.findById(lessonRequest.getLessonRequireId())
                                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                        EnumError.LESSON_REQUIRE_NOT_FOUND.getMessage() + " with id: "
                                                                        + lessonRequest.getLessonRequireId()));
                        lesson.setLessonRequireId(lessonRequire.getId());
                }

                Lesson savedLesson = lessonRepository.save(lesson);
                return lessonMapper.toLessonResponse(savedLesson);
        }

        @Override
        public LessonResponse getLessonDetails(Long lessonId) {
                Lesson lesson = lessonRepository.findById(lessonId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                EnumError.LESSON_NOT_FOUND.getMessage() + " with id: " + lessonId));

                return lessonMapper.toLessonResponse(lesson);
        }

        @Transactional
        @Override
        public LessonResponse updateLesson(Long lessonId, @Valid LessonRequest lessonRequest) {
                Lesson lesson = lessonRepository.findById(lessonId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                EnumError.LESSON_NOT_FOUND.getMessage() + " with id: " + lessonId));

                if (!lesson.getTitle().equals(lessonRequest.getTitle()) &&
                                lessonRepository.findByTitle(lessonRequest.getTitle()).isPresent()) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                        EnumError.LESSON_ALREADY_EXISTS.getMessage() + " with title: "
                                                        + lessonRequest.getTitle());
                }

                lessonMapper.updateLessonFromRequest(lessonRequest, lesson);

                // Update lessonRequire nếu có lessonRequireId
                if (lessonRequest.getLessonRequireId() != null) {
                        Lesson lessonRequire = lessonRepository.findById(lessonRequest.getLessonRequireId())
                                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                        EnumError.LESSON_REQUIRE_NOT_FOUND.getMessage() + " with id: "
                                                                        + lessonRequest.getLessonRequireId()));
                        lesson.setLessonRequireId(lessonRequire.getId());
                } else {
                        lesson.setLessonRequireId(null);
                }

                Lesson updatedLesson = lessonRepository.save(lesson);

                return lessonMapper.toLessonResponse(updatedLesson);
        }

        @Transactional
        @Override
        public String deleteLesson(Long lessonId) {
                Lesson lesson = lessonRepository.findById(lessonId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                EnumError.LESSON_NOT_FOUND.getMessage() + " with id: " + lessonId));

                lessonRepository.delete(lesson);
                return EnumSuccess.LESSON_DELETION_SUCCESS.getMessage();
        }

        @Override
        public List<LessonResponse> getLessonsByCourse(Long courseId) {
                Course course = courseRepository.findById(courseId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                EnumError.COURSE_NOT_FOUND.getMessage() + " with id: " + courseId));

                List<Lesson> lessons = lessonRepository.findByCourse(course);
                return lessonMapper.toLessonResponseList(lessons);
        }

        @Transactional
        @Override
        public String completeLesson(Long userId, Long lessonId) {
                UserEntity user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                EnumAuthError.USER_NOT_FOUND.getMessage() + " with id: " + userId));

                Lesson lesson = lessonRepository.findById(lessonId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                EnumError.LESSON_NOT_FOUND.getMessage() + " with id: " + lessonId));

                // Tạo/Update UserLesson - mark as completed
                UserLesson userLesson = userLessonRepository.findByUserAndLesson(user, lesson)
                                .orElse(null);

                if (userLesson == null) {
                        userLesson = new UserLesson();
                        userLesson.setUser(user);
                        userLesson.setLesson(lesson);
                }

                userLesson.setCompleted(true);
                userLesson.setCompletedDate(LocalDateTime.now());
                userLessonRepository.save(userLesson);

                // Tìm tất cả lesson có lessonRequire = lesson này
                List<Lesson> dependentLessons = lessonRepository.findByLessonRequire(lesson.getId());

                for (Lesson dependentLesson : dependentLessons) {
                        // Kiểm tra user đã complete lesson hiện tại (prerequisite) chưa
                        if (userLessonRepository.existsByUserAndLessonAndIsCompletedTrue(user, lesson)) {
                                // Tạo UserLesson cho dependent lesson và set isActive = true
                                UserLesson depUserLesson = userLessonRepository
                                                .findByUserAndLesson(user, dependentLesson)
                                                .orElse(null);

                                if (depUserLesson == null) {
                                        depUserLesson = new UserLesson();
                                        depUserLesson.setUser(user);
                                        depUserLesson.setLesson(dependentLesson);
                                }

                                depUserLesson.setActive(true);
                                userLessonRepository.save(depUserLesson);
                        }
                }

                return EnumSuccess.LESSON_COMPLETION_SUCCESS.getMessage();
        }

        @Override
        public List<UserLessonResponse> getUserCompletedLessons(Long userId) {
                UserEntity user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                EnumAuthError.USER_NOT_FOUND.getMessage() + " with id: " + userId));

                List<UserLesson> completedLessons = userLessonRepository
                                .findByUserAndIsCompletedTrue(user);
                return userLessonMapper.toUserLessonResponseList(completedLessons);
        }

        /**
         * Lấy danh sách lesson active của user
         */
        @Override
        public List<UserLessonResponse> getUserActiveLessons(Long userId) {
                UserEntity user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                EnumAuthError.USER_NOT_FOUND.getMessage() + " with id: " + userId));

                List<UserLesson> activeLessons = userLessonRepository
                                .findByUserAndIsActiveTrue(user);
                return userLessonMapper.toUserLessonResponseList(activeLessons);
        }

}
