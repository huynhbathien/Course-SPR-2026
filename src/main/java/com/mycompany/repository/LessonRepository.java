package com.mycompany.repository;

import com.mycompany.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    Optional<Lesson> findByTitle(String title);

    boolean existsByTitle(String title);

    List<Lesson> findByCourseId(Long courseId);

    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId ORDER BY l.createdAt ASC")
    List<Lesson> findByCourseIdOrderByCreatedAt(@Param("courseId") Long courseId);

    @Query("SELECT l FROM Lesson l WHERE l.title LIKE %:keyword%")
    List<Lesson> searchByKeyword(@Param("keyword") String keyword);

    long countByCourseId(Long courseId);
}

