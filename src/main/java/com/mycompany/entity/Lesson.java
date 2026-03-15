package com.mycompany.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lessons", indexes = {
        @Index(name = "idx_lesson_title", columnList = "title"),
        @Index(name = "idx_lesson_course", columnList = "course_id")
})
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
     * Tracks user progress for this lesson.
     */
    @OneToMany(mappedBy = "lesson", cascade = {
            CascadeType.PERSIST, CascadeType.MERGE
    }, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserLesson> userLessons = new ArrayList<>();

}
