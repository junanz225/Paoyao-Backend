package com.zhaojunan.paoyao_backend.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a player in the single Paoyao room.
 * - playerId: stable UUID used to identify the player (useful for logs/reconnect later)
 * - name: display name provided by the frontend
 * - session: the active WebSocketSession (may be null if not connected)
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {

    private UUID id;
    private String name;
    private WebSocketSession session;
    private List<Card> hand = new ArrayList<>();

    public void removeCards(List<Card> cards) {
        if (!hand.containsAll(cards)) {
            throw new IllegalArgumentException("Invalid play: cards not in hand");
        }
        hand.removeAll(cards);
    }

    public int getHandSize() {
        return hand.size();
    }

}
