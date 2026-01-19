package com.mycompany.service.Impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.mycompany.dto.request.CourseRequest;
import com.mycompany.dto.response.CourseGroupResponse;
import com.mycompany.dto.response.CourseResponse;
import com.mycompany.entity.Course;
import com.mycompany.entity.CourseType;
import com.mycompany.entity.UserCourse;
import com.mycompany.entity.UserEntity;
import com.mycompany.mapstruct.CourseMapper;
import com.mycompany.repository.CourseRepository;
import com.mycompany.repository.UserCourseRepository;
import com.mycompany.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService Tests")
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCourseRepository userCourseRepository;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course course;
    private CourseRequest courseRequest;
    private CourseResponse courseResponse;
    private UserEntity user;
    private CourseType courseType;

    @BeforeEach
    void setUp() {
        // Setup CourseType
        courseType = new CourseType();
        courseType.setId(1L);
        courseType.setCode("BASIC");
        courseType.setDescription("Basic Courses");

        // Setup Course
        course = new Course();
        course.setId(1L);
        course.setTitle("Spring Boot Basics");
        course.setDescription("Learn Spring Boot from basics");
        course.setLinkImg("https://example.com/image.jpg");
        course.setType(courseType);
        course.setLessons(new ArrayList<>());
        course.setUserCourses(new ArrayList<>());
        course.setCreatedAt(LocalDateTime.now());

        // Setup CourseRequest
        courseRequest = new CourseRequest();
        courseRequest.setTitle("Spring Boot Basics");
        courseRequest.setDescription("Learn Spring Boot from basics");
        courseRequest.setLinkImg("https://example.com/image.jpg");
        courseRequest.setType("BASIC");

        // Setup CourseResponse
        courseResponse = new CourseResponse();
        courseResponse.setId(1L);
        courseResponse.setTitle("Spring Boot Basics");
        courseResponse.setDescription("Learn Spring Boot from basics");
        courseResponse.setLinkImg("https://example.com/image.jpg");
        courseResponse.setType("BASIC");
        courseResponse.setTotalLessons(0);

        // Setup User
        user = new UserEntity();
        user.setId(1L);
        user.setUsername("testuser");
        user.setUserCourses(new ArrayList<>());
    }

    @Test
    @DisplayName("Should get course details successfully")
    void testGetCourseDetails() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseMapper.toCourseResponse(course)).thenReturn(courseResponse);

        // Act
        CourseResponse result = courseService.getCourseDetails(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Spring Boot Basics", result.getTitle());
        verify(courseRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when course not found")
    void testGetCourseDetails_NotFound() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            courseService.getCourseDetails(1L);
        });
    }

    @Test
    @DisplayName("Should create course successfully")
    void testCreateCourse() {
        // Arrange
        when(courseRepository.findByTitle("Spring Boot Basics")).thenReturn(Optional.empty());
        when(courseMapper.toCourseEntity(courseRequest)).thenReturn(course);
        when(courseRepository.save(course)).thenReturn(course);
        when(courseMapper.toCourseResponse(course)).thenReturn(courseResponse);

        // Act
        CourseResponse result = courseService.createCourse(courseRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Spring Boot Basics", result.getTitle());
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Should throw exception when course title already exists")
    void testCreateCourse_TitleAlreadyExists() {
        // Arrange
        when(courseRepository.findByTitle("Spring Boot Basics")).thenReturn(Optional.of(course));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            courseService.createCourse(courseRequest);
        });
    }

    @Test
    @DisplayName("Should update course successfully")
    void testUpdateCourse() {
        // Arrange
        CourseRequest updateRequest = new CourseRequest();
        updateRequest.setTitle("Spring Boot Advanced");
        updateRequest.setDescription("Advanced Spring Boot");
        updateRequest.setLinkImg("https://example.com/new.jpg");
        updateRequest.setType("ADVANCED");

        Course updatedCourse = new Course();
        updatedCourse.setId(1L);
        updatedCourse.setTitle("Spring Boot Advanced");
        updatedCourse.setDescription("Advanced Spring Boot");
        updatedCourse.setLinkImg("https://example.com/new.jpg");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.findByTitle("Spring Boot Advanced")).thenReturn(Optional.empty());
        when(courseRepository.save(course)).thenReturn(updatedCourse);
        when(courseMapper.toCourseResponse(updatedCourse)).thenReturn(courseResponse);

        // Act
        CourseResponse result = courseService.updateCourse(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Should throw exception when updating to existing title")
    void testUpdateCourse_TitleAlreadyExists() {
        // Arrange
        Course anotherCourse = new Course();
        anotherCourse.setId(2L);
        anotherCourse.setTitle("Other Course");

        CourseRequest updateRequest = new CourseRequest();
        updateRequest.setTitle("Other Course");
        updateRequest.setDescription("Updated");
        updateRequest.setLinkImg("url");
        updateRequest.setType("BASIC");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.findByTitle("Other Course")).thenReturn(Optional.of(anotherCourse));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            courseService.updateCourse(1L, updateRequest);
        });
    }

    @Test
    @DisplayName("Should delete course successfully")
    void testDeleteCourse() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        // Act
        String result = courseService.deleteCourse(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("deleted successfully"));
        verify(courseRepository).delete(course);
    }

    @Test
    @DisplayName("Should list all courses grouped by type")
    void testListAllCourses() {
        // Arrange
        List<Course> courses = Arrays.asList(course);

        when(courseRepository.findAllWithType()).thenReturn(courses);
        when(courseMapper.toCourseResponse(course)).thenReturn(courseResponse);

        // Act
        List<CourseGroupResponse> result = courseService.listAllCourses();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("BASIC", result.get(0).getCourseTypeCode());
        verify(courseRepository).findAllWithType();
    }

    @Test
    @DisplayName("Should purchase course successfully - new purchase")
    void testPurchaseCourse_NewPurchase() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userCourseRepository.findByUserAndCourse(user, course)).thenReturn(Optional.empty());
        when(courseMapper.toCourseResponse(course)).thenReturn(courseResponse);

        // Act
        CourseResponse result = courseService.purchaseCourse(1L, 1L);

        // Assert
        assertNotNull(result);
        verify(userCourseRepository).save(any(UserCourse.class));
    }

    @Test
    @DisplayName("Should purchase course successfully - already purchased")
    void testPurchaseCourse_AlreadyPurchased() {
        // Arrange
        UserCourse existingUserCourse = new UserCourse();
        existingUserCourse.setUser(user);
        existingUserCourse.setCourse(course);
        existingUserCourse.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userCourseRepository.findByUserAndCourse(user, course))
                .thenReturn(Optional.of(existingUserCourse));
        when(courseMapper.toCourseResponse(course)).thenReturn(courseResponse);

        // Act
        CourseResponse result = courseService.purchaseCourse(1L, 1L);

        // Assert
        assertNotNull(result);
        assertTrue(existingUserCourse.isActive());
        verify(userCourseRepository).save(existingUserCourse);
    }

    @Test
    @DisplayName("Should throw exception when user not found for purchase")
    void testPurchaseCourse_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            courseService.purchaseCourse(1L, 1L);
        });
    }

    @Test
    @DisplayName("Should throw exception when course not found for purchase")
    void testPurchaseCourse_CourseNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            courseService.purchaseCourse(1L, 1L);
        });
    }
}
