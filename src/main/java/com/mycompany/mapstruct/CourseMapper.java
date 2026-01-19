package com.mycompany.mapstruct;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.mycompany.config.MapStructConfig;
import com.mycompany.dto.request.CourseRequest;
import com.mycompany.dto.response.CourseResponse;
import com.mycompany.entity.Course;
import com.mycompany.entity.CourseType;
import com.mycompany.repository.CourseTypeRepository;

@Mapper(config = MapStructConfig.class)
public abstract class CourseMapper {

    // Sẽ được inject bởi Spring
    protected CourseTypeRepository courseTypeRepository;

    @Mapping(target = "type", source = "type.code")
    @Mapping(target = "totalLessons", source = "lessons", qualifiedByName = "lessonsToTotal")
    public abstract CourseResponse toCourseResponse(Course course);

    @Mapping(target = "type", source = "type.code")
    @Mapping(target = "totalLessons", source = "lessons", qualifiedByName = "lessonsToTotal")
    public abstract List<CourseResponse> toCourseResponseList(List<Course> courses);

    @Mapping(target = "type", source = "type", qualifiedByName = "mapStringToCourseType")
    public abstract Course toCourseEntity(CourseRequest courseRequest);

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "userCourses", ignore = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "linkImg", source = "linkImg")
    @Mapping(target = "description", source = "description")
    public abstract void updateCourseFromRequest(CourseRequest courseRequest, @MappingTarget Course course);

    @Named("lessonsToTotal")
    protected int lessonsToTotal(java.util.List<com.mycompany.entity.Lesson> lessons) {
        return lessons != null ? lessons.size() : 0;
    }

    /**
     * Map String courseType code thành CourseType entity
     */
    @Named("mapStringToCourseType")
    protected CourseType mapStringToCourseType(String courseTypeCode) {
        if (courseTypeCode == null) {
            return null;
        }
        // Tìm CourseType từ database bằng code
        return courseTypeRepository.findByCode(courseTypeCode).orElse(null);
    }
}
