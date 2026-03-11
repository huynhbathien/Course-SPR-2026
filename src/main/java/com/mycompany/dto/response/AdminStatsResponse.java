package com.mycompany.dto.response;

import java.util.List;

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
public class AdminStatsResponse {

    // User statistics
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long adminCount;
    private long regularUserCount;

    // Course statistics
    private long totalCourses;
    private long publishedCourses;
    private long draftCourses;

    // Enrollment statistics
    private long totalActiveEnrollments;

    // Top enrolled courses
    private List<CourseEnrollmentStat> topCoursesByEnrollment;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseEnrollmentStat {
        private Long courseId;
        private String courseTitle;
        private long enrollmentCount;
    }
}
