package com.zhaojunan.paoyao_backend.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response sent to all players when the game starts.
 */
@Data
@Builder
public class GameStartResponse {

    private String type; // "game_start"
    private List<PlayerDTO> payload;

}
