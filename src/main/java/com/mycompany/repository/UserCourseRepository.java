package com.mycompany.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mycompany.entity.Course;
import com.mycompany.entity.UserCourse;
import com.mycompany.entity.UserEntity;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {

    /**
     * Find UserCourse by user and course
     */
    Optional<UserCourse> findByUserAndCourse(UserEntity user, Course course);

    /**
     * Get active courses of a user
     */
    List<UserCourse> findByUserAndIsActiveTrue(UserEntity user);

    /**
     * Get all courses of a user (active and inactive)
     */
    List<UserCourse> findByUser(UserEntity user);

    /**
     * Get users who have purchased the course
     */
    List<UserCourse> findByCourseAndIsActiveTrue(Course course);

    /**
     * Count active enrollments for a course
     */
    long countByCourseAndIsActiveTrue(Course course);

    /**
     * Count active enrollments for a user (avoids lazy-loading getUserCourses())
     */
    long countByUser(UserEntity user);

    /**
     * Total number of active enrollments
     */
    long countByIsActiveTrue();
}
