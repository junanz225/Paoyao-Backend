package com.zhaojunan.paoyao_backend.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response sent to all players whenever the player list changes.
 */
@Data
@Builder
public class PlayerListResponse {

    private String type;
    private List<PlayerDTO> payload;

}
