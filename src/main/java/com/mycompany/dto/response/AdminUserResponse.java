package com.mycompany.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private boolean active;
    private boolean emailVerified;
    private String provider;
    private String avatarUrl;
    private int enrolledCourses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
