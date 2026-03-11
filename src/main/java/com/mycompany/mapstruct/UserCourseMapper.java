package com.mycompany.mapstruct;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mycompany.config.MapStructConfig;
import com.mycompany.dto.response.UserCourseResponse;
import com.mycompany.entity.UserCourse;

@Mapper(config = MapStructConfig.class)
public interface UserCourseMapper {

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseTitle", source = "course.title")
    @Mapping(target = "courseType", source = "course.type.code")
    UserCourseResponse toUserCourseResponse(UserCourse userCourse);

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseTitle", source = "course.title")
    @Mapping(target = "courseType", source = "course.type.code")
    List<UserCourseResponse> toUserCourseResponseList(List<UserCourse> userCourses);
}
