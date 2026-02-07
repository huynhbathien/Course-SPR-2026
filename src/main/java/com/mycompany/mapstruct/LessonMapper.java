package com.mycompany.mapstruct;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.mycompany.config.MapStructConfig;
import com.mycompany.dto.request.LessonRequest;
import com.mycompany.dto.response.LessonResponse;
import com.mycompany.entity.Lesson;

@Mapper(config = MapStructConfig.class)
public abstract class LessonMapper {

    @Mapping(target = "courseId", source = "lesson.course.id")
    public abstract LessonResponse toLessonResponse(Lesson lesson);

    public abstract List<LessonResponse> toLessonResponseList(List<Lesson> lessons);

    @Mapping(target = "course", ignore = true)
    @Mapping(target = "userLessons", ignore = true)
    public abstract Lesson toLessonEntity(LessonRequest lessonRequest);

    @Mapping(target = "course", ignore = true)
    @Mapping(target = "userLessons", ignore = true)
    public abstract void updateLessonFromRequest(LessonRequest lessonRequest, @MappingTarget Lesson lesson);
}
