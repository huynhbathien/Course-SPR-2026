package com.mycompany.mapper;

import com.mycompany.config.MapStructConfig;
import com.mycompany.dto.request.UserDTO;
import com.mycompany.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface ToUserEntityMapper {
    UserEntity toUserEntity(UserDTO userDTO);
}
