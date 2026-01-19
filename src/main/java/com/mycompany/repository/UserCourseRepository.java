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
     * Tìm UserCourse bằng user và course
     */
    Optional<UserCourse> findByUserAndCourse(UserEntity user, Course course);

    /**
     * Lấy danh sách course active của user
     */
    List<UserCourse> findByUserAndIsActiveTrue(UserEntity user);

    /**
     * Lấy tất cả course của user (active và inactive)
     */
    List<UserCourse> findByUser(UserEntity user);

    /**
     * Lấy danh sách user đã mua course
     */
    List<UserCourse> findByCourseAndIsActiveTrue(Course course);
}
