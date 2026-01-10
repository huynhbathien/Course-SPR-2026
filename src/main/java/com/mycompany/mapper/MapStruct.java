package com.mycompany.mapper;

import com.mycompany.config.MapStructConfig;
import com.mycompany.dto.request.RegisterRequestDTO;
import com.mycompany.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface MapStruct {
    UserEntity toUserEntity(RegisterRequestDTO userDTO);
}
