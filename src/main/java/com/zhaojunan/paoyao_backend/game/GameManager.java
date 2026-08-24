package com.zhaojunan.paoyao_backend.game;

import com.zhaojunan.paoyao_backend.exception.GameActionException;
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
        log.info("Player '{}' joined the room", name);
        return room.getPlayer(session);
    }

    public void leave(WebSocketSession session) {
        room.removePlayer(session);
    }

    public Player playCards(WebSocketSession session, List<Card> cards) {
        try {
            Player player = validateTurn(session);
            player.removeCards(cards);
            room.addToTable(cards);
            room.setLastPlayedPlayerId(player.getId());
            room.registerPlay();
            room.advanceTurn();
            log.info("Player '{}' played: {}", player.getName(),
                    cards.stream().map(Card::toString).toList());
            return player;
        } catch (GameActionException e) {
            sendError(session, e.getMessage());
            return null;
        }
    }

    public Player pass(WebSocketSession session) {
        try {
            Player player = validateTurn(session);
            room.registerPass();
            room.advanceTurn();
            log.info("Player '{}' passed", player.getName());
            return player;
        } catch (GameActionException e) {
            sendError(session, e.getMessage());
            return null;
        }
    }

    public Collection<Player> getPlayers() {
        return room.getPlayers();
    }

    private Player validateTurn(WebSocketSession session) {
        Player player = room.getPlayer(session);
        if (player == null) {
            log.warn("Play/pass rejected: no player found for session {}", session.getId());
            throw new GameActionException("Player not found");
        }
        if (!room.getCurrentPlayerId().equals(player.getId())) {
            log.warn("Player '{}' tried to act but it's not '{}''s turn",
                    player.getName(), player.getName());
            throw new GameActionException("Not your turn");
        }
        return player;
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
