package com.mycompany.service.Impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.mycompany.dto.request.CourseRequest;
import com.mycompany.dto.response.CourseGroupResponse;
import com.mycompany.dto.response.CourseResponse;
import com.mycompany.entity.Course;
import com.mycompany.entity.CourseType;
import com.mycompany.entity.Lesson;
import com.mycompany.mapper.CourseMapper;
import com.mycompany.repository.CourseRepository;

/**
 * Test class for CourseServiceImpl
 * Tests all business logic for course operations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CourseServiceImpl Tests")
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course course;
    private CourseType courseType;
    private CourseRequest courseRequest;
    private CourseResponse courseResponse;

    @BeforeEach
    void setUp() {
        // Initialize CourseType
        courseType = new CourseType();
        courseType.setId(1L);
        courseType.setCode("BACKEND");
        courseType.setDescription("Backend development courses");

        // Initialize Course entity
        course = new Course();
        course.setId(1L);
        course.setTitle("Spring Boot Basics");
        course.setType(courseType);
        course.setLinkImg("https://example.com/spring-boot.jpg");
        course.setDescription("Learn Spring Boot fundamentals");
        course.setActive(true);
        course.setLessons(new ArrayList<>());
        course.setUsers(new ArrayList<>());

        // Initialize CourseRequest DTO
        courseRequest = new CourseRequest(
                "Spring Boot Basics",
                "BACKEND",
                "https://example.com/spring-boot.jpg",
                "Learn Spring Boot fundamentals");

        // Initialize CourseResponse DTO
        courseResponse = new CourseResponse(
                "1",
                "Spring Boot Basics",
                "BACKEND",
                "https://example.com/spring-boot.jpg",
                "Learn Spring Boot fundamentals",
                0);
    }

    // ==================== GET COURSE DETAILS TESTS ====================

    @Test
    @DisplayName("getCourseDetails - Should return course when found")
    void testGetCourseDetails_Success() {
        // Arrange
        Long courseId = 1L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseMapper.toCourseResponse(course)).thenReturn(courseResponse);

        // Act
        CourseResponse result = courseService.getCourseDetails(courseId);

        // Assert
        assertNotNull(result);
        assertEquals("Spring Boot Basics", result.getTitle());
        assertEquals("BACKEND", result.getType());
        verify(courseRepository, times(1)).findById(courseId);
        verify(courseMapper, times(1)).toCourseResponse(course);
    }

    @Test
    @DisplayName("getCourseDetails - Should throw 404 when course not found")
    void testGetCourseDetails_NotFound() {
        // Arrange
        Long courseId = 999L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.getCourseDetails(courseId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Course not found"));
        verify(courseRepository, times(1)).findById(courseId);
    }

    // ==================== CREATE COURSE TESTS ====================

    @Test
    @DisplayName("createCourse - Should create new course successfully")
    void testCreateCourse_Success() {
        // Arrange
        when(courseRepository.findByTitle(courseRequest.getTitle())).thenReturn(Optional.empty());
        when(courseMapper.toCourseEntity(courseRequest)).thenReturn(course);
        when(courseRepository.save(course)).thenReturn(course);
        when(courseMapper.toCourseResponse(course)).thenReturn(courseResponse);

        // Act
        CourseResponse result = courseService.createCourse(courseRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Spring Boot Basics", result.getTitle());
        verify(courseRepository, times(1)).findByTitle(courseRequest.getTitle());
        verify(courseRepository, times(1)).save(course);
        verify(courseMapper, times(1)).toCourseEntity(courseRequest);
        verify(courseMapper, times(1)).toCourseResponse(course);
    }

    @Test
    @DisplayName("createCourse - Should throw 409 when title already exists")
    void testCreateCourse_TitleAlreadyExists() {
        // Arrange
        when(courseRepository.findByTitle(courseRequest.getTitle())).thenReturn(Optional.of(course));

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.createCourse(courseRequest));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("already exists"));
        verify(courseRepository, times(1)).findByTitle(courseRequest.getTitle());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("createCourse - Should handle null request")
    void testCreateCourse_NullRequest() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> courseService.createCourse(null));
    }

    // ==================== UPDATE COURSE TESTS ====================

    @Test
    @DisplayName("updateCourse - Should update course successfully")
    void testUpdateCourse_Success() {
        // Arrange
        Long courseId = 1L;
        CourseRequest updateRequest = new CourseRequest(
                "Spring Boot Advanced",
                "BACKEND",
                "https://example.com/advanced.jpg",
                "Learn advanced Spring Boot");

        Course updatedCourse = new Course();
        updatedCourse.setId(1L);
        updatedCourse.setTitle("Spring Boot Advanced");
        updatedCourse.setType(courseType);
        updatedCourse.setLinkImg("https://example.com/advanced.jpg");
        updatedCourse.setDescription("Learn advanced Spring Boot");
        updatedCourse.setActive(true);
        updatedCourse.setLessons(new ArrayList<>());
        updatedCourse.setUsers(new ArrayList<>());

        CourseResponse updatedResponse = new CourseResponse(
                "1",
                "Spring Boot Advanced",
                "BACKEND",
                "https://example.com/advanced.jpg",
                "Learn advanced Spring Boot",
                0);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.findByTitle(updateRequest.getTitle())).thenReturn(Optional.empty());
        when(courseRepository.save(any(Course.class))).thenReturn(updatedCourse);
        when(courseMapper.toCourseResponse(updatedCourse)).thenReturn(updatedResponse);

        // Act
        CourseResponse result = courseService.updateCourse(courseId, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Spring Boot Advanced", result.getTitle());
        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, times(1)).findByTitle(updateRequest.getTitle());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    @DisplayName("updateCourse - Should throw 404 when course not found")
    void testUpdateCourse_CourseNotFound() {
        // Arrange
        Long courseId = 999L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.updateCourse(courseId, courseRequest));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("updateCourse - Should throw 409 when new title already exists")
    void testUpdateCourse_TitleAlreadyExists() {
        // Arrange
        Long courseId = 1L;
        Course existingCourse = new Course();
        existingCourse.setId(2L);
        existingCourse.setTitle("Spring Boot Advanced");

        CourseRequest updateRequest = new CourseRequest(
                "Spring Boot Advanced",
                "BACKEND",
                "https://example.com/advanced.jpg",
                "Learn advanced Spring Boot");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.findByTitle(updateRequest.getTitle())).thenReturn(Optional.of(existingCourse));

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.updateCourse(courseId, updateRequest));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, times(1)).findByTitle(updateRequest.getTitle());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("updateCourse - Should allow same title update")
    void testUpdateCourse_SameTitleAllowed() {
        // Arrange
        Long courseId = 1L;
        CourseRequest sameRequest = new CourseRequest(
                "Spring Boot Basics", // Same title
                "BACKEND",
                "https://example.com/updated.jpg",
                "Updated description");

        Course updatedCourse = new Course();
        updatedCourse.setId(1L);
        updatedCourse.setTitle("Spring Boot Basics");
        updatedCourse.setType(courseType);
        updatedCourse.setLinkImg("https://example.com/updated.jpg");
        updatedCourse.setDescription("Updated description");
        updatedCourse.setActive(true);
        updatedCourse.setLessons(new ArrayList<>());
        updatedCourse.setUsers(new ArrayList<>());

        CourseResponse updatedResponse = new CourseResponse(
                "1",
                "Spring Boot Basics",
                "BACKEND",
                "https://example.com/updated.jpg",
                "Updated description",
                0);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(updatedCourse);
        when(courseMapper.toCourseResponse(updatedCourse)).thenReturn(updatedResponse);

        // Act
        CourseResponse result = courseService.updateCourse(courseId, sameRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated description", result.getDescription());
        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    // ==================== DELETE COURSE TESTS ====================

    @Test
    @DisplayName("deleteCourse - Should delete course successfully")
    void testDeleteCourse_Success() {
        // Arrange
        Long courseId = 1L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        doNothing().when(courseRepository).delete(course);

        // Act
        String result = courseService.deleteCourse(courseId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("deleted successfully"));
        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, times(1)).delete(course);
    }

    @Test
    @DisplayName("deleteCourse - Should throw 404 when course not found")
    void testDeleteCourse_NotFound() {
        // Arrange
        Long courseId = 999L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> courseService.deleteCourse(courseId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, never()).delete(any(Course.class));
    }

    // ==================== LIST COURSES TESTS ====================

    @Test
    @DisplayName("listAllCourses - Should return grouped courses by type")
    void testListAllCourses_Success() {
        // Arrange
        Course backendCourse1 = new Course();
        backendCourse1.setId(1L);
        backendCourse1.setTitle("Spring Boot");
        backendCourse1.setType(courseType);
        backendCourse1.setLinkImg("image1.jpg");
        backendCourse1.setDescription("desc1");
        backendCourse1.setActive(true);
        backendCourse1.setLessons(new ArrayList<>());
        backendCourse1.setUsers(new ArrayList<>());

        CourseType frontendType = new CourseType();
        frontendType.setId(2L);
        frontendType.setCode("FRONTEND");
        frontendType.setDescription("Frontend development");

        Course frontendCourse = new Course();
        frontendCourse.setId(3L);
        frontendCourse.setTitle("React Basics");
        frontendCourse.setType(frontendType);
        frontendCourse.setLinkImg("image3.jpg");
        frontendCourse.setDescription("desc3");
        frontendCourse.setActive(true);
        frontendCourse.setLessons(new ArrayList<>());
        frontendCourse.setUsers(new ArrayList<>());

        List<Course> allCourses = List.of(course, backendCourse1, frontendCourse);

        CourseResponse response1 = new CourseResponse("1", "Spring Boot Basics", "BACKEND", "image1.jpg", "desc1", 0);
        CourseResponse response2 = new CourseResponse("2", "Spring Boot", "BACKEND", "image2.jpg", "desc2", 0);
        CourseResponse response3 = new CourseResponse("3", "React Basics", "FRONTEND", "image3.jpg", "desc3", 0);

        when(courseRepository.findAllWithType()).thenReturn(allCourses);
        when(courseMapper.toCourseResponse(any(Course.class)))
                .thenReturn(response1)
                .thenReturn(response2)
                .thenReturn(response3);

        // Act
        List<CourseGroupResponse> result = courseService.listAllCourses();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify BACKEND group
        CourseGroupResponse backendGroup = result.stream()
                .filter(g -> g.getCourseTypeCode().equals("BACKEND"))
                .findFirst()
                .orElse(null);
        assertNotNull(backendGroup);
        assertEquals(2, backendGroup.getCourses().size());

        // Verify FRONTEND group
        CourseGroupResponse frontendGroup = result.stream()
                .filter(g -> g.getCourseTypeCode().equals("FRONTEND"))
                .findFirst()
                .orElse(null);
        assertNotNull(frontendGroup);
        assertEquals(1, frontendGroup.getCourses().size());

        verify(courseRepository, times(1)).findAllWithType();
    }

    @Test
    @DisplayName("listAllCourses - Should return empty list when no courses exist")
    void testListAllCourses_Empty() {
        // Arrange
        when(courseRepository.findAllWithType()).thenReturn(new ArrayList<>());

        // Act
        List<CourseGroupResponse> result = courseService.listAllCourses();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(courseRepository, times(1)).findAllWithType();
    }

    @Test
    @DisplayName("listAllCourses - Should handle single course")
    void testListAllCourses_SingleCourse() {
        // Arrange
        List<Course> courses = List.of(course);
        CourseResponse response = new CourseResponse("1", "Spring Boot Basics", "BACKEND", "image.jpg", "desc", 0);

        when(courseRepository.findAllWithType()).thenReturn(courses);
        when(courseMapper.toCourseResponse(course)).thenReturn(response);

        // Act
        List<CourseGroupResponse> result = courseService.listAllCourses();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("BACKEND", result.get(0).getCourseTypeCode());
        assertEquals(1, result.get(0).getCourses().size());
        verify(courseRepository, times(1)).findAllWithType();
    }
}
