package com.zhaojunan.paoyao_backend.game;

import com.zhaojunan.paoyao_backend.model.entity.Player;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.zhaojunan.paoyao_backend.model.entity.Card;

import java.util.Collection;
import java.util.List;

@Component
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

    public void playCards(WebSocketSession session, List<Card> cards) {
        Player player = room.getPlayer(session);
        // TODO: add turn/phase validation here
        player.removeCards(cards);   // delegate to Player
        room.addToTable(cards);      // a new method to track table cards
        room.setLastPlayedPlayerId(player.getId());
    }

    public Collection<Player> getPlayers() {
        return room.getPlayers();
    }

}
