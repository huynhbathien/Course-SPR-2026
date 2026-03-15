package com.mycompany.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mycompany.entity.CourseType;

@Repository
public interface CourseTypeRepository extends JpaRepository<CourseType, Long> {

    /**
     * Find CourseType by code
     */
    Optional<CourseType> findByCode(String code);

    /**
     * Check whether code already exists
     */
    boolean existsByCode(String code);

    /**
     * Find CourseType by description
     */
    Optional<CourseType> findByDescription(String description);
}
