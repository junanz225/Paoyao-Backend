package com.zhaojunan.paoyao_backend.game;

import com.zhaojunan.paoyao_backend.model.entity.Card;
import com.zhaojunan.paoyao_backend.model.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameRoom {

    private static final int MAX_PLAYERS = 4;

    // The 4 players in the room
    private final List<Player> players = new ArrayList<>();

    // Game state flags
    private boolean gameStarted = false;

    public synchronized boolean addPlayer(Player player) {
        if (players.size() >= MAX_PLAYERS || gameStarted) {
            return false; // room full or game already started
        }
        players.add(player);
        return true;
    }

    public synchronized void removePlayer(Player player) {
        players.remove(player);

        // If any player leaves, end/reset the game
        if (gameStarted) {
            resetGame();
        }
    }

    public synchronized List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public synchronized boolean isRoomFull() {
        return players.size() == MAX_PLAYERS;
    }

    public synchronized boolean hasStarted() {
        return gameStarted;
    }

    public synchronized void startGame() {
        if (isRoomFull() && !gameStarted) {
            gameStarted = true;

            Deck deck = new Deck();
            deck.shuffle();
            for (Player player : players) {
                List<Card> hand = deck.deal(27);
                player.setHand(hand);
            }
        }
    }

    public synchronized void resetGame() {
        gameStarted = false;
        players.clear();
    }

}
