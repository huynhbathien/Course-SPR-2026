package com.mycompany.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lesson extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false, length = 5000, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "lesson_require_id")
    private Long lessonRequireId;

    /**
     * Tracking tiến độ học của user với lesson này
     * Thay thế @ManyToMany users để tránh N+1 query
     */
    @OneToMany(mappedBy = "lesson", cascade = {
            CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE }, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserLesson> userLessons = new ArrayList<>();

}
