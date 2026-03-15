package com.mycompany.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mycompany.dto.request.UpdateUserRoleRequest;
import com.mycompany.dto.request.UpdateUserStatusRequest;
import com.mycompany.dto.response.AdminCourseResponse;
import com.mycompany.dto.response.AdminStatsResponse;
import com.mycompany.dto.response.AdminUserResponse;
import com.mycompany.enums.EnumCourseStatus;

public interface AdminService {

    // ── User Management ──────────────────────────────────────────────────────

    /**
     * Get all users (paginated, optionally filtered by role/active).
     */
    Page<AdminUserResponse> getAllUsers(String role, Boolean active, Pageable pageable);

    /**
     * Get detailed user information by ID.
     */
    AdminUserResponse getUserById(Long userId);

    /**
     * Enable or disable a user's active status.
     */
    AdminUserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request);

    /**
     * Change a user's role.
     */
    AdminUserResponse updateUserRole(Long userId, UpdateUserRoleRequest request);

    // ── Course Management ─────────────────────────────────────────────────────

    /**
     * Get courses (paginated, optionally filtered by status).
     */
    Page<AdminCourseResponse> getAllCourses(EnumCourseStatus status, Pageable pageable);

    /**
     * Publish course (DRAFT → PUBLISHED).
     */
    AdminCourseResponse publishCourse(Long courseId);

    /**
     * Unpublish course (PUBLISHED → DRAFT).
     */
    AdminCourseResponse unpublishCourse(Long courseId);

    // ── Statistics ────────────────────────────────────────────────────────────

    /**
     * Get summary statistics for the admin dashboard.
     */
    AdminStatsResponse getDashboardStats();
}
