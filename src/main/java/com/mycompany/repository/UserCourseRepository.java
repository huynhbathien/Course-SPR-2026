package com.mycompany.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycompany.entity.UserCourse;

public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {
    UserCourse findByUserIdAndCourseId(Long userId, Long courseId);
}
