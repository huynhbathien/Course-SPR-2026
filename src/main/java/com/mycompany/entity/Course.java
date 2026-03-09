package com.mycompany.entity;

import java.util.ArrayList;
import java.util.List;

import com.mycompany.enums.EnumCourseStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Course extends BaseEntity {

        @Column(unique = true, nullable = false)
        private String title;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "course_type_id", nullable = false)
        private CourseType type;

        @Column(name = "link_Img")
        String linkImg;

        @Column(columnDefinition = "TEXT")
        private String description;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        private EnumCourseStatus status = EnumCourseStatus.DRAFT;

        @OneToMany(mappedBy = "course", cascade = {
                        CascadeType.PERSIST, CascadeType.MERGE,
                        CascadeType.REMOVE }, orphanRemoval = true, fetch = FetchType.LAZY)
        private List<Lesson> lessons = new ArrayList<>();

        @OneToMany(mappedBy = "course", cascade = {
                        CascadeType.PERSIST, CascadeType.MERGE,
                        CascadeType.REMOVE }, orphanRemoval = true, fetch = FetchType.LAZY)
        private List<UserCourse> userCourses = new ArrayList<>();

}
