package com.zhaojunan.paoyao_backend.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequest {

    private String type; // "join"
    private String name; // player display name

}
