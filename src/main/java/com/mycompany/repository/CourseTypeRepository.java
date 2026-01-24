package com.mycompany.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mycompany.entity.CourseType;

@Repository
public interface CourseTypeRepository extends JpaRepository<CourseType, Long> {

    /**
     * Tìm CourseType theo code
     */
    Optional<CourseType> findByCode(String code);

    /**
     * Kiểm tra code đã tồn tại chưa
     */
    boolean existsByCode(String code);

    /**
     * Tìm CourseType theo description
     */
    Optional<CourseType> findByDescription(String description);
}
