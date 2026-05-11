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

    // Primary store — keyed by stable player ID
    private final Map<UUID, Player> idToPlayer = new HashMap<>();

    // Lookup index — maps live session → player ID
    private final Map<WebSocketSession, UUID> sessionToId = new HashMap<>();

    private final List<UUID> seatOrder = new ArrayList<>(); // stable turn order

    @Getter @Setter
    private UUID currentPlayerId;

    @Getter @Setter
    private UUID lastPlayedPlayerId;

    @Getter @Setter
    private List<Card> table = new ArrayList<>();

    @Getter @Setter
    private int tablePoints = 0;

    private boolean gameStarted = false;

    // -------------------------------------------------------------------------
    // Player lifecycle
    // -------------------------------------------------------------------------

    public synchronized boolean addPlayer(WebSocketSession session, String name) {
        if (gameStarted) {
            // Game is running — only allow reconnect by matching name
            for (Player p : idToPlayer.values()) {
                if (p.getName().equals(name)) {
                    // Swap in the new session, discard the old one
                    sessionToId.remove(p.getSession());
                    p.setSession(session);
                    sessionToId.put(session, p.getId());
                    return true;
                }
            }
            // Unknown name while game is running — reject
            return false;
        }

        // Pre-game: normal join
        if (idToPlayer.size() >= MAX_PLAYERS) {
            return false;
        }

        Player player = Player.builder()
                .id(UUID.randomUUID())
                .name(name)
                .session(session)
                .build();

        idToPlayer.put(player.getId(), player);
        sessionToId.put(session, player.getId());
        seatOrder.add(player.getId());

        return true;
    }

    public synchronized void removePlayer(WebSocketSession session) {
        UUID id = sessionToId.remove(session);

        if (!gameStarted) {
            // Pre-game disconnect — fully remove the player
            if (id != null) {
                idToPlayer.remove(id);
            }
        }
        // Mid-game disconnect — keep the player's hand/state intact,
        // just drop the session reference so they can reconnect later
    }

    public synchronized Player getPlayer(WebSocketSession session) {
        UUID id = sessionToId.get(session);
        if (id == null) return null;
        return idToPlayer.get(id);
    }

    public synchronized Collection<Player> getPlayers() {
        return Collections.unmodifiableCollection(idToPlayer.values());
    }

    // -------------------------------------------------------------------------
    // Room / game state
    // -------------------------------------------------------------------------

    public synchronized boolean isRoomFull() {
        return idToPlayer.size() == MAX_PLAYERS;
    }

    public synchronized boolean hasStarted() {
        return gameStarted;
    }

    public synchronized void addToTable(List<Card> cards) {
        table.clear();
        table.addAll(cards);
    }

    public synchronized void startGame() {
        if (isRoomFull() && !gameStarted) {
            gameStarted = true;
            table.clear();
            Deck deck = new Deck();
            deck.shuffle();
            for (Player player : idToPlayer.values()) {
                player.setHand(deck.deal(27));
            }
            currentPlayerId = seatOrder.get(0);
        }
    }

    public synchronized void advanceTurn() {
        int currentIndex = seatOrder.indexOf(currentPlayerId);
        int nextIndex = (currentIndex + 1) % seatOrder.size();
        currentPlayerId = seatOrder.get(nextIndex);
    }

    public synchronized void resetGame() {
        gameStarted = false;
        idToPlayer.clear();
        sessionToId.clear();
        table.clear();
        seatOrder.clear();
        tablePoints = 0;
        lastPlayedPlayerId = null;
    }
}