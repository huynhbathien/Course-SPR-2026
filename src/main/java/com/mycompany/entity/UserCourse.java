package com.mycompany.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCourse extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Column(name = "progress")
    private int progress; // 0-100

}
