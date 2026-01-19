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
     * Tìm UserLesson theo user và lesson
     */
    Optional<UserLesson> findByUserAndLesson(UserEntity user, Lesson lesson);

    /**
     * Lấy danh sách lesson completed của user
     */
    List<UserLesson> findByUserAndIsCompletedTrue(UserEntity user);

    /**
     * Lấy danh sách lesson active của user
     */
    List<UserLesson> findByUserAndIsActiveTrue(UserEntity user);

    /**
     * Lấy tất cả lesson của user
     */
    List<UserLesson> findByUser(UserEntity user);

    /**
     * Lấy tất cả lesson của user trong 1 course
     */
    List<UserLesson> findByUserAndLessonCourse(UserEntity user,
            com.mycompany.entity.Course course);

    /**
     * Kiểm tra user đã complete lesson chưa
     */
    boolean existsByUserAndLessonAndIsCompletedTrue(UserEntity user, Lesson lesson);
}
