package com.mycompany.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mycompany.entity.Lesson;
import com.mycompany.entity.UserEntity;
import com.mycompany.entity.UserLesson;

@Repository
public interface UserLessonRepository extends JpaRepository<UserLesson, Long> {

    /**
     * Find UserLesson by user and lesson
     */
    Optional<UserLesson> findByUserAndLesson(UserEntity user, Lesson lesson);

    /**
     * Get completed lessons of a user
     * Spring Data JPA convention: field "isCompleted" → Method "CompletedTrue"
     */
    List<UserLesson> findByUserAndCompletedTrue(UserEntity user);

    /**
     * Get active lessons of a user
     * Spring Data JPA convention: field "isActive" → Method "ActiveTrue"
     */
    List<UserLesson> findByUserAndActiveTrue(UserEntity user);

    /**
     * Get all lessons of a user
     */
    List<UserLesson> findByUser(UserEntity user);

    /**
     * Get all lessons of a user in one course
     */
    List<UserLesson> findByUserAndLessonCourse(UserEntity user,
            com.mycompany.entity.Course course);

    /**
     * Check whether user has completed the lesson
     * Spring Data JPA convention: field "isCompleted" → Method "CompletedTrue"
     */
    boolean existsByUserAndLessonAndCompletedTrue(UserEntity user, Lesson lesson);
}
