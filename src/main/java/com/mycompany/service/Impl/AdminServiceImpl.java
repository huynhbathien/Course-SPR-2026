package com.mycompany.service.Impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

import com.mycompany.dto.request.UpdateUserRoleRequest;
import com.mycompany.dto.request.UpdateUserStatusRequest;
import com.mycompany.dto.response.AdminCourseResponse;
import com.mycompany.dto.response.AdminStatsResponse;
import com.mycompany.dto.response.AdminStatsResponse.CourseEnrollmentStat;
import com.mycompany.dto.response.AdminUserResponse;
import com.mycompany.entity.Course;
import com.mycompany.entity.UserEntity;
import com.mycompany.enums.EnumCourseStatus;
import com.mycompany.enums.EnumRole;
import com.mycompany.exception.AppException;
import com.mycompany.repository.CourseRepository;
import com.mycompany.repository.LessonRepository;
import com.mycompany.repository.UserCourseRepository;
import com.mycompany.repository.UserRepository;
import com.mycompany.service.AdminService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminServiceImpl implements AdminService {

    UserRepository userRepository;
    CourseRepository courseRepository;
    UserCourseRepository userCourseRepository;
    LessonRepository lessonRepository;

    // ─────────────────────────── User Management ───────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getAllUsers(String role, Boolean active, Pageable pageable) {
        Page<UserEntity> users;

        if (role != null && active != null) {
            users = userRepository.findByRoleAndActive(role, active, pageable);
        } else if (role != null) {
            users = userRepository.findByRole(role, pageable);
        } else if (active != null) {
            users = userRepository.findByActive(active, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::toAdminUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long userId) {
        UserEntity user = findUserOrThrow(userId);
        return toAdminUserResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserStatus(Long userId, @Valid UpdateUserStatusRequest request) {
        UserEntity user = findUserOrThrow(userId);
        user.setActive(request.getActive());
        UserEntity saved = userRepository.save(user);
        log.info("Admin updated user {} active status to {}", userId, request.getActive());
        return toAdminUserResponse(saved);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserRole(Long userId, @Valid UpdateUserRoleRequest request) {
        UserEntity user = findUserOrThrow(userId);

        // Normalize: accept both "ADMIN" and "ROLE_ADMIN" from client
        String normalised = request.getRole().toUpperCase();
        if (normalised.startsWith("ROLE_")) {
            normalised = normalised.substring(5);
        }
        try {
            EnumRole.valueOf(normalised);
        } catch (IllegalArgumentException e) {
            throw new AppException(
                    com.mycompany.enums.EnumError.INVALID_ROLE.getCode(),
                    com.mycompany.enums.EnumError.INVALID_ROLE.getMessage());
        }

        user.setRole(EnumRole.valueOf(normalised).getRoleName());
        UserEntity saved = userRepository.save(user);
        log.info("Admin changed user {} role to {}", userId, request.getRole());
        return toAdminUserResponse(saved);
    }

    // ─────────────────────────── Course Management ─────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCourseResponse> getAllCourses(EnumCourseStatus status, Pageable pageable) {
        Page<Course> courses = (status != null)
                ? courseRepository.findByStatus(status, pageable)
                : courseRepository.findAll(pageable);

        return courses.map(this::toAdminCourseResponse);
    }

    @Override
    @Transactional
    public AdminCourseResponse publishCourse(Long courseId) {
        Course course = findCourseOrThrow(courseId);
        if (course.getStatus() == EnumCourseStatus.PUBLISHED) {
            throw new AppException(
                    com.mycompany.enums.EnumError.COURSE_ALREADY_PUBLISHED.getCode(),
                    com.mycompany.enums.EnumError.COURSE_ALREADY_PUBLISHED.getMessage());
        }
        course.setStatus(EnumCourseStatus.PUBLISHED);
        Course saved = courseRepository.save(course);
        log.info("Admin published course {}", courseId);
        return toAdminCourseResponse(saved);
    }

    @Override
    @Transactional
    public AdminCourseResponse unpublishCourse(Long courseId) {
        Course course = findCourseOrThrow(courseId);
        if (course.getStatus() == EnumCourseStatus.DRAFT) {
            throw new AppException(
                    com.mycompany.enums.EnumError.COURSE_ALREADY_DRAFT.getCode(),
                    com.mycompany.enums.EnumError.COURSE_ALREADY_DRAFT.getMessage());
        }
        course.setStatus(EnumCourseStatus.DRAFT);
        Course saved = courseRepository.save(course);
        log.info("Admin unpublished course {}", courseId);
        return toAdminCourseResponse(saved);
    }

    // ─────────────────────────── Statistics ────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActive(true);
        long adminCount = userRepository.countByRole(EnumRole.ADMIN.getRoleName());

        long totalCourses = courseRepository.count();
        long publishedCourses = courseRepository.countByStatus(EnumCourseStatus.PUBLISHED);
        long draftCourses = courseRepository.countByStatus(EnumCourseStatus.DRAFT);

        long totalActiveEnrollments = userCourseRepository.countByIsActiveTrue();

        // Top 5 courses by enrollment — single GROUP BY query (avoids N+1)
        List<CourseEnrollmentStat> topCourses = courseRepository.findTopCoursesByEnrollment(PageRequest.of(0, 5))
                .stream()
                .map(row -> CourseEnrollmentStat.builder()
                        .courseId((Long) row[0])
                        .courseTitle((String) row[1])
                        .enrollmentCount((Long) row[2])
                        .build())
                .collect(Collectors.toList());

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(totalUsers - activeUsers)
                .adminCount(adminCount)
                .regularUserCount(totalUsers - adminCount)
                .totalCourses(totalCourses)
                .publishedCourses(publishedCourses)
                .draftCourses(draftCourses)
                .totalActiveEnrollments(totalActiveEnrollments)
                .topCoursesByEnrollment(topCourses)
                .build();
    }

    // ─────────────────────────── Private helpers ───────────────────────────

    private UserEntity findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        com.mycompany.enums.EnumAuthError.USER_NOT_FOUND.getCode(),
                        com.mycompany.enums.EnumAuthError.USER_NOT_FOUND.getMessage()));
    }

    private Course findCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(
                        com.mycompany.enums.EnumError.COURSE_NOT_FOUND.getCode(),
                        com.mycompany.enums.EnumError.COURSE_NOT_FOUND.getMessage()));
    }

    private AdminUserResponse toAdminUserResponse(UserEntity user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .provider(user.getProvider())
                .avatarUrl(user.getAvatarUrl())
                .enrolledCourses((int) userCourseRepository.countByUser(user))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private AdminCourseResponse toAdminCourseResponse(Course course) {
        long enrollmentCount = userCourseRepository.countByCourseAndIsActiveTrue(course);
        return AdminCourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .type(course.getType() != null ? course.getType().getCode() : null)
                .linkImg(course.getLinkImg())
                .description(course.getDescription())
                .totalLessons((int) lessonRepository.countByCourse(course))
                .status(course.getStatus())
                .enrollmentCount(enrollmentCount)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
