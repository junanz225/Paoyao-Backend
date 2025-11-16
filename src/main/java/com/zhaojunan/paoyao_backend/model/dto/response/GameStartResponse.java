package com.zhaojunan.paoyao_backend.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Response sent to all players when the game starts.
 */
@Data
@AllArgsConstructor
public class GameStartResponse {

    private String type; // "game_start"
    private List<PlayerDTO> payload;

}
