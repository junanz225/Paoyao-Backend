package com.zhaojunan.paoyao_backend.mapper;

import com.zhaojunan.paoyao_backend.model.dto.response.PlayerDTO;
import com.zhaojunan.paoyao_backend.model.entity.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PlayerMapper {

    PlayerMapper INSTANCE = Mappers.getMapper(PlayerMapper.class);

    @Mapping(source = "id",   target = "playerId")
    @Mapping(source = "name", target = "playerName")
    PlayerDTO toDTO(Player player);

    @Mapping(source = "playerId",   target = "id")
    @Mapping(source = "playerName", target = "name")
    Player toEntity(PlayerDTO dto);

}
