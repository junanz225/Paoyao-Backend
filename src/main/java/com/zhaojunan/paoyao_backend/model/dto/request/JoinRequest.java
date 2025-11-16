package com.zhaojunan.paoyao_backend.model.dto.request;

import lombok.Builder;
import lombok.Data;

/**
 * Request sent by the frontend when a player wants to join the game room.
 * Example JSON:
 * {
 *   "type": "join",
 *   "name": "Alice"
 * }
 */
@Data
@Builder
public class JoinRequest {

    private String type; // "join"
    private String name; // player display name

}
