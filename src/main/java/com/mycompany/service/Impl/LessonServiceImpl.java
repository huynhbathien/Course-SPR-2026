package com.mycompany.service.Impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import com.mycompany.dto.request.LessonRequest;
import com.mycompany.dto.response.LessonResponse;
import com.mycompany.entity.Course;
import com.mycompany.entity.Lesson;
import com.mycompany.enums.EnumError;
import com.mycompany.enums.EnumSuccess;
import com.mycompany.mapstruct.LessonMapper;
import com.mycompany.repository.CourseRepository;
import com.mycompany.repository.LessonRepository;
import com.mycompany.service.LessonService;
import com.mycompany.util.QueryUtils;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Validated
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final LessonMapper lessonMapper;

    @Transactional
    @Override
    public LessonResponse createLesson(@Valid LessonRequest lessonRequest) {
        Course course = courseRepository.findById(lessonRequest.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        EnumError.COURSE_NOT_FOUND.getMessage() + " with id: " + lessonRequest.getCourseId()));

        if (lessonRepository.findByTitle(lessonRequest.getTitle()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    EnumError.LESSON_ALREADY_EXISTS.getMessage() + " with title: " + lessonRequest.getTitle());
        }

        Lesson lesson = lessonMapper.toLessonEntity(lessonRequest);
        lesson.setCourse(course);

        if (lessonRequest.getLessonRequireId() != null) {
            Lesson lessonRequire = lessonRepository.findById(lessonRequest.getLessonRequireId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            EnumError.LESSON_REQUIRE_NOT_FOUND.getMessage() + " with id: "
                                    + lessonRequest.getLessonRequireId()));

            if (hasCircularDependency(lessonRequire, null)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Circular dependency detected: Lesson " + lessonRequire.getId()
                                + " cannot have " + lesson.getId() + " as prerequisite");
            }

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

        if (!lesson.getTitle().equals(lessonRequest.getTitle())
                && lessonRepository.findByTitle(lessonRequest.getTitle()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    EnumError.LESSON_ALREADY_EXISTS.getMessage() + " with title: " + lessonRequest.getTitle());
        }

        lessonMapper.updateLessonFromRequest(lessonRequest, lesson);

        if (lessonRequest.getLessonRequireId() != null) {
            Lesson lessonRequire = lessonRepository.findById(lessonRequest.getLessonRequireId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            EnumError.LESSON_REQUIRE_NOT_FOUND.getMessage() + " with id: "
                                    + lessonRequest.getLessonRequireId()));

            if (hasCircularDependency(lessonRequire, new java.util.HashSet<>(java.util.List.of(lessonId)))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Circular dependency detected: Lesson " + lessonRequire.getId()
                                + " cannot have " + lesson.getId() + " as prerequisite");
            }

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
    public Page<LessonResponse> searchLessons(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search keyword cannot be empty");
        }
        String escaped = QueryUtils.escapeLikeKeyword(keyword.trim());
        return lessonRepository.searchByKeyword(escaped, pageable).map(lessonMapper::toLessonResponse);
    }

    @Override
    public List<LessonResponse> getLessonsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        EnumError.COURSE_NOT_FOUND.getMessage() + " with id: " + courseId));

        List<Lesson> lessons = lessonRepository.findByCourse(course);
        return lessonMapper.toLessonResponseList(lessons);
    }

    private boolean hasCircularDependency(Lesson lesson, java.util.Set<Long> visited) {
        if (visited == null) {
            visited = new java.util.HashSet<>();
        }

        if (visited.contains(lesson.getId())) {
            return true;
        }

        if (lesson.getLessonRequireId() == null) {
            return false;
        }

        visited.add(lesson.getId());
        Lesson requiredLesson = lessonRepository.findById(lesson.getLessonRequireId()).orElse(null);

        if (requiredLesson == null) {
            return false;
        }

        return hasCircularDependency(requiredLesson, visited);
    }
}