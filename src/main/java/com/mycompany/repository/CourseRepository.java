package com.mycompany.repository;

import com.mycompany.enums.EnumCourseStatus;

import com.mycompany.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT c FROM Course c WHERE c.title LIKE %:keyword% ESCAPE '!'")
    Page<Course> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Course c ORDER BY c.createdAt DESC")
    List<Course> findAllByNewest();

    @Query("Select c from Course c JOIN FETCH c.type")
    List<Course> findAllWithType();

    // Admin queries
    Page<Course> findAll(Pageable pageable);

    Page<Course> findByStatus(EnumCourseStatus status, Pageable pageable);

    long countByStatus(EnumCourseStatus status);

    /**
     * Top N courses by active enrollment count — single GROUP BY query (no N+1).
     * Returns Object[]{courseId, courseTitle, enrollmentCount}.
     */
    @Query("SELECT c.id, c.title, COUNT(uc) FROM Course c " +
            "LEFT JOIN c.userCourses uc ON uc.isActive = true " +
            "GROUP BY c.id, c.title ORDER BY COUNT(uc) DESC")
    List<Object[]> findTopCoursesByEnrollment(Pageable pageable);
}
