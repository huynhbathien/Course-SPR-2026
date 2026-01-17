package com.mycompany.mapper;

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

@Mapper(config = MapStructConfig.class)
public abstract class CourseMapper {

    @Mapping(target = "type", source = "type.code")
    @Mapping(target = "id", source = "id", qualifiedByName = "longToString")
    @Mapping(target = "totalLessons", source = "lessons", qualifiedByName = "lessonsToTotal")
    public abstract CourseResponse toCourseResponse(Course course);

    public abstract List<CourseResponse> toCourseResponseList(List<Course> courses);

    public Course toCourseEntity(CourseRequest courseRequest) {
        if (courseRequest == null) {
            return null;
        }
        Course course = new Course();
        course.setTitle(courseRequest.getTitle());
        course.setLinkImg(courseRequest.getLinkImg());
        course.setDescription(courseRequest.getDescription());
        course.setActive(true);

        CourseType courseType = new CourseType();
        courseType.setCode(courseRequest.getType());
        course.setType(courseType);

        return course;
    }

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "users", ignore = true)
    public abstract void updateCourseFromRequest(CourseRequest courseRequest, @MappingTarget Course course);

    @Named("longToString")
    protected String longToString(Long id) {
        return id != null ? String.valueOf(id) : null;
    }

    @Named("lessonsToTotal")
    protected int lessonsToTotal(java.util.List<com.mycompany.entity.Lesson> lessons) {
        return lessons != null ? lessons.size() : 0;
    }
}
