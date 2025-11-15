package com.zhaojunan.paoyao_backend.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Response sent to all players whenever the player list changes.
 */
@Data
@AllArgsConstructor
public class PlayerListResponse {

    private String type;
    private List<PlayerDto> payload;

}
