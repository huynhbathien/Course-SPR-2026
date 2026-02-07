package com.mycompany.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLesson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "is_active", nullable = false)
    private boolean active = false;

    @Column(name = "is_completed", nullable = false)
    private boolean completed = false;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "score")
    private Integer score;
}
