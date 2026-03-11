package com.mycompany.mapstruct;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mycompany.config.MapStructConfig;
import com.mycompany.dto.response.UserLessonResponse;
import com.mycompany.entity.UserLesson;

@Mapper(config = MapStructConfig.class)
public interface UserLessonMapper {

    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "lessonTitle", source = "lesson.title")
    UserLessonResponse toUserLessonResponse(UserLesson userLesson);

    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "lessonTitle", source = "lesson.title")
    List<UserLessonResponse> toUserLessonResponseList(List<UserLesson> userLessons);
}
