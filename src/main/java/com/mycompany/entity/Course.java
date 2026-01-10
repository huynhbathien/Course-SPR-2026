package com.mycompany.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String title;

    @Column(name = "type", nullable = false)
    String type;

    @Column(name = "link_Img")
    String linkImg;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons = new ArrayList<>();

    @ManyToMany(mappedBy = "courses")
    private List<UserEntity> users = new ArrayList<>();
}
