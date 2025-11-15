package com.zhaojunan.paoyao_backend.model.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

/**
 * Represents a player in the single Paoyao room.
 * - playerId: stable UUID used to identify the player (useful for logs/reconnect later)
 * - name: display name provided by the frontend
 * - session: the active WebSocketSession (may be null if not connected)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {

    private String playerId;
    private String name;
    private WebSocketSession session;

}
