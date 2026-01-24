package com.mycompany.service.Impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.mycompany.dto.request.CourseRequest;
import com.mycompany.dto.response.CourseGroupResponse;
import com.mycompany.dto.response.CourseResponse;
import com.mycompany.entity.Course;
import com.mycompany.entity.UserCourse;
import com.mycompany.entity.UserEntity;
import com.mycompany.enums.EnumError;
import com.mycompany.mapstruct.CourseMapper;
import com.mycompany.mapstruct.UserCourseMapper;
import com.mycompany.repository.CourseRepository;
import com.mycompany.repository.UserCourseRepository;
import com.mycompany.repository.UserRepository;
import com.mycompany.service.CourseService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;
    private final CourseMapper courseMapper;
    private final UserCourseMapper userCourseMapper;

    @Override
    public CourseResponse getCourseDetails(Long courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                EnumError.COURSE_NOT_FOUND.getMessage() + " with id: " + courseId));
        CourseResponse courseResponse = courseMapper.toCourseResponse(course);
        return courseResponse;
    }

    @Transactional
    @Override
    public CourseResponse createCourse(@Valid CourseRequest courseData) {
        if (courseRepository.findByTitle(courseData.getTitle()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    EnumError.COURSE_ALREADY_EXISTS.getMessage() + " with title: " + courseData.getTitle());
        }
        Course newCourse = courseMapper.toCourseEntity(courseData);
        Course savedCourseEntity = courseRepository.save(newCourse);
        CourseResponse savedCourse = courseMapper.toCourseResponse(savedCourseEntity);

        return savedCourse;
    }

    @Transactional
    @Override
    public CourseResponse updateCourse(Long courseId, CourseRequest courseData) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                EnumError.COURSE_NOT_FOUND.getMessage() + " with id: " + courseId));

        // Check if title is already taken by another course
        if (!course.getTitle().equals(courseData.getTitle()) &&
                courseRepository.findByTitle(courseData.getTitle()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    EnumError.COURSE_ALREADY_EXISTS.getMessage() + " with title: " + courseData.getTitle());
        }

        // Map and update
        course.setTitle(courseData.getTitle());
        course.setLinkImg(courseData.getLinkImg());
        course.setDescription(courseData.getDescription());

        Course updatedCourse = courseRepository.save(course);
        CourseResponse courseResponse = courseMapper.toCourseResponse(updatedCourse);

        return courseResponse;
    }

    @Transactional
    @Override
    public String deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                EnumError.COURSE_NOT_FOUND.getMessage() + " with id: " + courseId));

        courseRepository.delete(course);
        return "Course with id: " + courseId + " deleted successfully";
    }

    @Override
    public List<CourseGroupResponse> listAllCourses() {
        List<Course> courses = courseRepository.findAllWithType();

        Map<String, List<CourseResponse>> groupedCourses = courses.stream().collect(
                Collectors.groupingBy(
                        c -> c.getType().getCode(),
                        Collectors.mapping(courseMapper::toCourseResponse, Collectors.toList())));

        List<CourseGroupResponse> courseGroupResponses = new ArrayList<>();
        for (Map.Entry<String, List<CourseResponse>> entry : groupedCourses.entrySet()) {
            CourseGroupResponse groupResponse = new CourseGroupResponse();
            groupResponse.setCourseTypeCode(entry.getKey());
            groupResponse.setCourses(entry.getValue());
            courseGroupResponses.add(groupResponse);
        }
        return courseGroupResponses;
    }

    /**
     * User mua khóa học - tạo record UserCourse với isActive = true
     */
    // @Transactional
    // public CourseResponse purchaseCourse(Long userId, Long courseId) {
    // UserEntity user = userRepository.findById(userId)
    // .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
    // "User not found with id: " + userId));

    // Course course = courseRepository.findById(courseId)
    // .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
    // EnumError.COURSE_NOT_FOUND.getMessage() + " with id: " + courseId));

    // // Kiểm tra user đã mua khóa học này chưa
    // UserCourse userCourse =
    // userCourseRepository.findByUserIdAndCourseId(user.getId(), course.getId());

    // if (userCourse != null) {
    // throw new ResponseStatusException(HttpStatus.CONFLICT,
    // "User with id: " + userId + " has already purchased course with id: " +
    // courseId);
    // }

    // // Tạo mới record UserCourse

    // return courseMapper.toCourseResponse(course);
    // }

}
