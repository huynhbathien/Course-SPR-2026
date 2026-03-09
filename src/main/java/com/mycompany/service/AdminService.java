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
     * Lấy danh sách tất cả user (có phân trang, lọc theo role/active).
     */
    Page<AdminUserResponse> getAllUsers(String role, Boolean active, Pageable pageable);

    /**
     * Lấy thông tin chi tiết một user theo ID.
     */
    AdminUserResponse getUserById(Long userId);

    /**
     * Bật / tắt trạng thái active của user.
     */
    AdminUserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request);

    /**
     * Thay đổi role của user.
     */
    AdminUserResponse updateUserRole(Long userId, UpdateUserRoleRequest request);

    // ── Course Management ─────────────────────────────────────────────────────

    /**
     * Lấy danh sách course (có phân trang, lọc theo status).
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
     * Lấy thống kê tổng quan cho admin dashboard.
     */
    AdminStatsResponse getDashboardStats();
}
