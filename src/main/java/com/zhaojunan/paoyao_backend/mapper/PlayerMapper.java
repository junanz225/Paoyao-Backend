package com.zhaojunan.paoyao_backend.mapper;

import com.zhaojunan.paoyao_backend.model.dto.response.PlayerDTO;
import com.zhaojunan.paoyao_backend.model.entity.Player;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PlayerMapper {

    PlayerMapper INSTANCE = Mappers.getMapper(PlayerMapper.class);

    PlayerDTO toDTO(Player player);

    Player toEntity(PlayerDTO dto);

}
