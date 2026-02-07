package com.mycompany.mapstruct;

import org.mapstruct.Mapper;

import com.mycompany.config.MapStructConfig;
import com.mycompany.dto.request.RegisterRequestDTO;
import com.mycompany.entity.UserEntity;

@Mapper(config = MapStructConfig.class)
public interface UserMapper {

    UserEntity toUserEntity(RegisterRequestDTO registerRequestDTO);
}
