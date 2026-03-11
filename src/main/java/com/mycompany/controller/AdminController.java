package com.mycompany.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.dto.APIResponse;
import com.mycompany.dto.request.UpdateUserRoleRequest;
import com.mycompany.dto.request.UpdateUserStatusRequest;
import com.mycompany.dto.response.AdminCourseResponse;
import com.mycompany.dto.response.AdminStatsResponse;
import com.mycompany.dto.response.AdminUserResponse;
import com.mycompany.enums.EnumCourseStatus;
import com.mycompany.enums.EnumSuccess;
import com.mycompany.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    AdminService adminService;

    // ─────────────────────────── User Management ───────────────────────────

    /**
     * GET /admin/users
     * Lấy danh sách user có phân trang, lọc theo role và/hoặc active.
     */
    @GetMapping("/users")
    public APIResponse<Page<AdminUserResponse>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminUserResponse> data = adminService.getAllUsers(role, active, pageable);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), "Users retrieved successfully", data);
    }

    /**
     * GET /admin/users/{userId}
     * Lấy thông tin chi tiết một user.
     */
    @GetMapping("/users/{userId}")
    public APIResponse<AdminUserResponse> getUserById(@PathVariable Long userId) {
        AdminUserResponse data = adminService.getUserById(userId);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), "User retrieved successfully", data);
    }

    /**
     * PUT /admin/users/{userId}/status
     * Bật / tắt trạng thái active của user.
     */
    @PutMapping("/users/{userId}/status")
    public APIResponse<AdminUserResponse> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {

        AdminUserResponse data = adminService.updateUserStatus(userId, request);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(),
                "User status updated successfully", data);
    }

    /**
     * PUT /admin/users/{userId}/role
     * Thay đổi role của user.
     */
    @PutMapping("/users/{userId}/role")
    public APIResponse<AdminUserResponse> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        AdminUserResponse data = adminService.updateUserRole(userId, request);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(),
                "User role updated successfully", data);
    }

    // ─────────────────────────── Course Management ─────────────────────────

    /**
     * GET /admin/courses
     * Lấy danh sách course, lọc theo status (DRAFT, PUBLISHED).
     */
    @GetMapping("/courses")
    public APIResponse<Page<AdminCourseResponse>> getAllCourses(
            @RequestParam(required = false) EnumCourseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminCourseResponse> data = adminService.getAllCourses(status, pageable);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), "Courses retrieved successfully", data);
    }

    /**
     * PUT /admin/courses/{courseId}/publish
     * Publish course (DRAFT → PUBLISHED).
     */
    @PutMapping("/courses/{courseId}/publish")
    public APIResponse<AdminCourseResponse> publishCourse(@PathVariable Long courseId) {
        AdminCourseResponse data = adminService.publishCourse(courseId);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), "Course published successfully", data);
    }

    /**
     * PUT /admin/courses/{courseId}/unpublish
     * Unpublish course (PUBLISHED → DRAFT).
     */
    @PutMapping("/courses/{courseId}/unpublish")
    public APIResponse<AdminCourseResponse> unpublishCourse(@PathVariable Long courseId) {
        AdminCourseResponse data = adminService.unpublishCourse(courseId);
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(), "Course unpublished successfully", data);
    }

    // ─────────────────────────── Statistics ────────────────────────────────

    /**
     * GET /admin/stats
     * Lấy thống kê tổng quan: users, courses, enrollments.
     */
    @GetMapping("/stats")
    public APIResponse<AdminStatsResponse> getDashboardStats() {
        AdminStatsResponse data = adminService.getDashboardStats();
        return APIResponse.success(EnumSuccess.SUCCESS.getCode(),
                "Dashboard statistics retrieved successfully", data);
    }
}
