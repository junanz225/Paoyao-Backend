package com.zhaojunan.paoyao_backend.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response sent to a player after successfully joining the room.
 */
@Data
@AllArgsConstructor
public class JoinedResponse {

    private String type = "joined";
    private String playerId;
    private String playerName;

}
