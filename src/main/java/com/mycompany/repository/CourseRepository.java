package com.mycompany.repository;

import com.mycompany.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByTitle(String title);

    boolean existsByTitle(String title);

    List<Course> findByType(String type);

    @Query("SELECT c FROM Course c WHERE c.title LIKE %:keyword%")
    List<Course> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT c FROM Course c ORDER BY c.createdAt DESC")
    List<Course> findAllByNewest();
}
