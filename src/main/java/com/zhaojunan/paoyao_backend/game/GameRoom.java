package com.zhaojunan.paoyao_backend.game;

import com.zhaojunan.paoyao_backend.model.entity.Card;
import com.zhaojunan.paoyao_backend.model.entity.Player;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameRoom {

    private static final int MAX_PLAYERS = 4;

    // identity maps
    private final Map<WebSocketSession, Player> sessionToPlayer = new HashMap<>();

    @Getter
    @Setter
    private List<Card> tableCards = new ArrayList<>();

    @Getter
    @Setter
    private int tablePoints = 0;

    // Game state flags
    private boolean gameStarted = false;

    public synchronized boolean addPlayer(WebSocketSession session, String name) {
        if (sessionToPlayer.size() >= MAX_PLAYERS || gameStarted) {
            return false;
        }

        Player player = Player.builder()
                .id(UUID.randomUUID())
                .name(name)
                .session(session)
                .build();

        sessionToPlayer.put(session, player);

        return true;
    }

    public synchronized void removePlayer(WebSocketSession session) {
        sessionToPlayer.remove(session);

        // If any player leaves, end/reset the game
        if (gameStarted) {
            resetGame();
        }
    }

    public synchronized Player getPlayer(WebSocketSession session) {
        return sessionToPlayer.get(session);
    }


    public synchronized Collection<Player> getPlayers() {
        return Collections.unmodifiableCollection(sessionToPlayer.values());
    }

    public synchronized boolean isRoomFull() {
        return sessionToPlayer.size() == MAX_PLAYERS;
    }

    public synchronized boolean hasStarted() {
        return gameStarted;
    }

    public synchronized void startGame() {
        if (isRoomFull() && !gameStarted) {
            gameStarted = true;

            Deck deck = new Deck();
            deck.shuffle();
            for (Player player : sessionToPlayer.values()) {
                player.setHand(deck.deal(27));
            }
        }
    }

    public synchronized void resetGame() {
        gameStarted = false;
        sessionToPlayer.clear();
    }

}
