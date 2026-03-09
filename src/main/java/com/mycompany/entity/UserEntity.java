package com.mycompany.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", indexes = {
                @Index(name = "idx_username", columnList = "username", unique = true),
                @Index(name = "idx_email", columnList = "email", unique = true),
                @Index(name = "idx_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Column(unique = true, nullable = false, length = 100)
        private String username;

        @Column(unique = true, length = 150)
        private String email;

        @Column(nullable = false)
        private String password;

        @Column(nullable = false, length = 150)
        private String fullName;

        @Column(length = 50)
        private String role;

        @OneToMany(mappedBy = "user", cascade = {
                        CascadeType.PERSIST, CascadeType.MERGE
        }, orphanRemoval = true, fetch = FetchType.LAZY)
        private List<UserCourse> userCourses = new ArrayList<>();

        @OneToMany(mappedBy = "user", cascade = {
                        CascadeType.PERSIST, CascadeType.MERGE
        }, orphanRemoval = true, fetch = FetchType.LAZY)
        private List<UserLesson> userLessons = new ArrayList<>();

        @Column(nullable = false)
        private boolean active = true;

        @Column(length = 500)
        private String avatarUrl;

        // OAuth2 Fields
        @Column(unique = true)
        private String googleId;

        @Column(length = 50)
        private String provider; // google, github, facebook, etc.

        /**
         * Google refresh token — stored for future use cases that require calling
         * Google APIs on behalf of the user (e.g. calendar sync, Drive access)
         * without asking the user to re-authenticate.
         * No concrete use case yet, but retained to avoid a DB migration later.
         */
        @Column(columnDefinition = "TEXT")
        private String refreshToken;

        @Column(nullable = false)
        private boolean emailVerified = false;
}
