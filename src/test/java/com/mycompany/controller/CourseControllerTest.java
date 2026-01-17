package com.mycompany.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.dto.request.CourseRequest;
import com.mycompany.dto.response.CourseGroupResponse;
import com.mycompany.dto.response.CourseResponse;
import com.mycompany.service.CourseService;

/**
 * Test class for CourseController
 * Tests all CRUD operations and list functionality
 */
@WebMvcTest(CourseController.class)
@DisplayName("CourseController Tests")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @Autowired
    private ObjectMapper objectMapper;

    private CourseRequest courseRequest;
    private CourseResponse courseResponse;
    private List<CourseGroupResponse> courseGroupResponses;

    @BeforeEach
    void setUp() {
        // Initialize test data
        courseRequest = new CourseRequest(
                "Spring Boot Basics",
                "BACKEND",
                "https://example.com/spring-boot.jpg",
                "Learn Spring Boot fundamentals");

        courseResponse = new CourseResponse(
                "1",
                "Spring Boot Basics",
                "BACKEND",
                "https://example.com/spring-boot.jpg",
                "Learn Spring Boot fundamentals",
                5);

        // Initialize list response
        List<CourseResponse> backendCourses = Arrays.asList(
                courseResponse,
                new CourseResponse("2", "Spring Security", "BACKEND", "https://example.com/security.jpg",
                        "Learn security", 3));

        List<CourseResponse> frontendCourses = Arrays.asList(
                new CourseResponse("3", "React Basics", "FRONTEND", "https://example.com/react.jpg", "Learn React", 4));

        courseGroupResponses = new ArrayList<>();
        CourseGroupResponse backendGroup = new CourseGroupResponse("BACKEND", backendCourses);
        CourseGroupResponse frontendGroup = new CourseGroupResponse("FRONTEND", frontendCourses);
        courseGroupResponses.add(backendGroup);
        courseGroupResponses.add(frontendGroup);
    }

    // ==================== GET COURSE DETAILS TESTS ====================

    @Test
    @DisplayName("GET /course/{courseId} - Should return course details successfully")
    void testGetCourseDetail_Success() throws Exception {
        // Arrange
        Long courseId = 1L;
        when(courseService.getCourseDetails(courseId)).thenReturn(courseResponse);

        // Act & Assert
        mockMvc.perform(get("/course/{courseId}", courseId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.title").value("Spring Boot Basics"))
                .andExpect(jsonPath("$.data.type").value("BACKEND"))
                .andExpect(jsonPath("$.data.totalLessons").value(5));

        verify(courseService, times(1)).getCourseDetails(courseId);
    }

    @Test
    @DisplayName("GET /course/{courseId} - Should return 404 when course not found")
    void testGetCourseDetail_NotFound() throws Exception {
        // Arrange
        Long courseId = 999L;
        when(courseService.getCourseDetails(courseId))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Course not found with id: " + courseId));

        // Act & Assert
        mockMvc.perform(get("/course/{courseId}", courseId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).getCourseDetails(courseId);
    }

    // ==================== CREATE COURSE TESTS ====================

    @Test
    @DisplayName("POST /course - Should create new course successfully")
    void testCreateCourse_Success() throws Exception {
        // Arrange
        when(courseService.createCourse(any(CourseRequest.class))).thenReturn(courseResponse);

        // Act & Assert
        mockMvc.perform(post("/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.title").value("Spring Boot Basics"))
                .andExpect(jsonPath("$.data.type").value("BACKEND"));

        verify(courseService, times(1)).createCourse(any(CourseRequest.class));
    }

    @Test
    @DisplayName("POST /course - Should return 409 when title already exists")
    void testCreateCourse_TitleAlreadyExists() throws Exception {
        // Arrange
        when(courseService.createCourse(any(CourseRequest.class)))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT,
                        "Course already exists with title: " + courseRequest.getTitle()));

        // Act & Assert
        mockMvc.perform(post("/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isConflict());

        verify(courseService, times(1)).createCourse(any(CourseRequest.class));
    }

    @Test
    @DisplayName("POST /course - Should return 400 for invalid request (missing title)")
    void testCreateCourse_InvalidRequest_MissingTitle() throws Exception {
        // Arrange
        CourseRequest invalidRequest = new CourseRequest(
                null, // Missing title
                "BACKEND",
                "https://example.com/image.jpg",
                "Description");

        // Act & Assert
        mockMvc.perform(post("/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(courseService, never()).createCourse(any(CourseRequest.class));
    }

    @Test
    @DisplayName("POST /course - Should return 400 for invalid request (missing type)")
    void testCreateCourse_InvalidRequest_MissingType() throws Exception {
        // Arrange
        CourseRequest invalidRequest = new CourseRequest(
                "Spring Boot",
                null, // Missing type
                "https://example.com/image.jpg",
                "Description");

        // Act & Assert
        mockMvc.perform(post("/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(courseService, never()).createCourse(any(CourseRequest.class));
    }

    // ==================== UPDATE COURSE TESTS ====================

    @Test
    @DisplayName("PUT /course/{courseId} - Should update course successfully")
    void testUpdateCourse_Success() throws Exception {
        // Arrange
        Long courseId = 1L;
        CourseRequest updateRequest = new CourseRequest(
                "Spring Boot Advanced",
                "BACKEND",
                "https://example.com/advanced.jpg",
                "Learn advanced Spring Boot");

        CourseResponse updatedResponse = new CourseResponse(
                "1",
                "Spring Boot Advanced",
                "BACKEND",
                "https://example.com/advanced.jpg",
                "Learn advanced Spring Boot",
                5);

        when(courseService.updateCourse(courseId, updateRequest)).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/course/{courseId}", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.title").value("Spring Boot Advanced"));

        verify(courseService, times(1)).updateCourse(courseId, updateRequest);
    }

    @Test
    @DisplayName("PUT /course/{courseId} - Should return 404 when course not found")
    void testUpdateCourse_NotFound() throws Exception {
        // Arrange
        Long courseId = 999L;
        when(courseService.updateCourse(eq(courseId), any(CourseRequest.class)))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Course not found with id: " + courseId));

        // Act & Assert
        mockMvc.perform(put("/course/{courseId}", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).updateCourse(eq(courseId), any(CourseRequest.class));
    }

    @Test
    @DisplayName("PUT /course/{courseId} - Should return 400 for invalid request")
    void testUpdateCourse_InvalidRequest() throws Exception {
        // Arrange
        Long courseId = 1L;
        CourseRequest invalidRequest = new CourseRequest(
                "", // Empty title
                "BACKEND",
                "https://example.com/image.jpg",
                "Description");

        // Act & Assert
        mockMvc.perform(put("/course/{courseId}", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(courseService, never()).updateCourse(anyLong(), any(CourseRequest.class));
    }

    // ==================== DELETE COURSE TESTS ====================

    @Test
    @DisplayName("DELETE /course/{courseId} - Should delete course successfully")
    void testDeleteCourse_Success() throws Exception {
        // Arrange
        Long courseId = 1L;
        String successMessage = "Course with id: " + courseId + " deleted successfully";
        when(courseService.deleteCourse(courseId)).thenReturn(successMessage);

        // Act & Assert
        mockMvc.perform(delete("/course/{courseId}", courseId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").value(successMessage));

        verify(courseService, times(1)).deleteCourse(courseId);
    }

    @Test
    @DisplayName("DELETE /course/{courseId} - Should return 404 when course not found")
    void testDeleteCourse_NotFound() throws Exception {
        // Arrange
        Long courseId = 999L;
        when(courseService.deleteCourse(courseId))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Course not found with id: " + courseId));

        // Act & Assert
        mockMvc.perform(delete("/course/{courseId}", courseId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(courseService, times(1)).deleteCourse(courseId);
    }

    // ==================== LIST COURSES TESTS ====================

    @Test
    @DisplayName("GET /course/list - Should return all courses grouped by type")
    void testListAllCourses_Success() throws Exception {
        // Arrange
        when(courseService.listAllCourses()).thenReturn(courseGroupResponses);

        // Act & Assert
        mockMvc.perform(get("/course/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].courseTypeCode").value("BACKEND"))
                .andExpect(jsonPath("$.data[0].courses", hasSize(2)))
                .andExpect(jsonPath("$.data[0].courses[0].title").value("Spring Boot Basics"))
                .andExpect(jsonPath("$.data[1].courseTypeCode").value("FRONTEND"))
                .andExpect(jsonPath("$.data[1].courses", hasSize(1)))
                .andExpect(jsonPath("$.data[1].courses[0].title").value("React Basics"));

        verify(courseService, times(1)).listAllCourses();
    }

    @Test
    @DisplayName("GET /course/list - Should return empty list when no courses exist")
    void testListAllCourses_Empty() throws Exception {
        // Arrange
        when(courseService.listAllCourses()).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/course/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(courseService, times(1)).listAllCourses();
    }

    // ==================== EDGE CASES & ERROR HANDLING TESTS ====================

    @Test
    @DisplayName("GET /course/{courseId} - Should handle invalid courseId format")
    void testGetCourseDetail_InvalidIdFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/course/abc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /course - Should validate all required fields")
    void testCreateCourse_AllFieldsRequired() throws Exception {
        // Arrange
        String invalidJson = "{}"; // Empty request body

        // Act & Assert
        mockMvc.perform(post("/course")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(courseService, never()).createCourse(any(CourseRequest.class));
    }

    @Test
    @DisplayName("PUT /course/{courseId} - Should validate all required fields")
    void testUpdateCourse_AllFieldsRequired() throws Exception {
        // Arrange
        Long courseId = 1L;
        String invalidJson = "{}"; // Empty request body

        // Act & Assert
        mockMvc.perform(put("/course/{courseId}", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(courseService, never()).updateCourse(anyLong(), any(CourseRequest.class));
    }
}
