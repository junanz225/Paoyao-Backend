package com.zhaojunan.paoyao_backend.game;

import com.zhaojunan.paoyao_backend.model.entity.Player;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.zhaojunan.paoyao_backend.model.entity.Card;

import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class GameManager {

    @Getter
    private final GameRoom room = new GameRoom();

    public Player join(WebSocketSession session, String name) {
        boolean added = room.addPlayer(session, name);
        if (!added) return null;
        return room.getPlayer(session);
    }

    public void leave(WebSocketSession session) {
        room.removePlayer(session);
    }

    public Player playCards(WebSocketSession session, List<Card> cards) {
        Player player = room.getPlayer(session);
        if (player == null) {
            log.error("Player not found for session: {}", session.getId());
            sendError(session, "Player not found");
            return null;
        }

        try {
            // TODO: add turn/phase validation here

            player.removeCards(cards);
            room.addToTable(cards);
            room.setLastPlayedPlayerId(player.getId());

            return player;

        } catch (Exception e) {
            log.error(e.getMessage());
            sendError(session, e.getMessage());
            return null;
        }
    }

    public Collection<Player> getPlayers() {
        return room.getPlayers();
    }

    private void sendError(WebSocketSession session, String message) {
        try {
            session.sendMessage(
                    new TextMessage(errorJson(message))
            );
        } catch (Exception e) {
            log.error("Failed to send error message", e);
        }
    }

    private String errorJson(String message) {
        // Simple JSON for the frontend
        return String.format("{\"type\":\"error\",\"payload\":\"%s\"}", message);
    }

}
