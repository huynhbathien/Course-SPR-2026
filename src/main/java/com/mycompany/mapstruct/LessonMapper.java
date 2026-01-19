package com.mycompany.mapstruct;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.mycompany.config.MapStructConfig;
import com.mycompany.dto.request.LessonRequest;
import com.mycompany.dto.response.LessonResponse;
import com.mycompany.entity.Lesson;

@Mapper(config = MapStructConfig.class)
public abstract class LessonMapper {

    public abstract LessonResponse toLessonResponse(Lesson lesson);

    public abstract List<LessonResponse> toLessonResponseList(List<Lesson> lessons);

    public abstract Lesson toLessonEntity(LessonRequest lessonRequest);

    public abstract void updateLessonFromRequest(LessonRequest lessonRequest, @MappingTarget Lesson lesson);
}
