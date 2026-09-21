package com.zhaojunan.paoyao_backend.game;

import com.zhaojunan.paoyao_backend.model.entity.Card;
import com.zhaojunan.paoyao_backend.model.entity.Player;
import com.zhaojunan.paoyao_backend.model.enumeration.WinReason;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class GameRoom {

    private static final int MAX_PLAYERS = 4;

    // Primary store — keyed by stable player ID
    private final Map<UUID, Player> idToPlayer = new LinkedHashMap<>();

    // Lookup index — maps live session → player ID
    private final Map<WebSocketSession, UUID> sessionToId = new HashMap<>();

    private final List<UUID> seatOrder = new ArrayList<>(); // stable turn order

    @Getter @Setter
    private UUID currentPlayerId;

    @Getter @Setter
    private UUID lastPlayedPlayerId;

    @Getter
    private int passCount = 0;

    @Getter
    private UUID roundWinnerId; // set when a round completes; consumed by the handler

    // --- new fields ---
    private final Set<UUID> emptiedPlayers = new HashSet<>();
    private UUID firstEmptiedPlayerId;
    private Integer firstEmptiedTeam; // team of the first player ever to empty their hand

    @Getter
    private boolean gameOver = false;

    @Getter
    private Integer winningTeam;      // 0 or 1

    @Getter
    private WinReason winReason;         // "FIRST_EMPTIER_90" | "SCORE_140" | "DOUBLE_OUT"

    @Getter @Setter
    private List<Card> table = new ArrayList<>();

    @Getter
    private int tablePoints = 0;

    private final Map<Integer, Integer> teamScores = new HashMap<>();

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
        tablePoints += cards.stream().mapToInt(Card::getPoint).sum();
    }

    public synchronized void startGame() {
        if (isRoomFull() && !gameStarted) {
            gameStarted = true;
            table.clear();
            Deck deck = new Deck();
            deck.shuffle();

            teamScores.put(0, 0);
            teamScores.put(1, 0);

            for (int i = 0; i < seatOrder.size(); i++) {
                Player player = idToPlayer.get(seatOrder.get(i));
                player.setHand(deck.deal(27));
                player.setTeam(i % 2); // seats 0,2 -> team 0; seats 1,3 -> team 1
            }

            currentPlayerId = seatOrder.get(0);
            log.info("Game started. Seat order: {}",
                    seatOrder.stream()
                            .map(id -> idToPlayer.get(id).getName())
                            .toList());
        }
    }

    public synchronized void advanceTurn() {
        int currentIndex = seatOrder.indexOf(currentPlayerId);
        int nextIndex = currentIndex;
        int attempts = 0;
        do {
            nextIndex = (nextIndex + 1) % seatOrder.size();
            attempts++;
        } while (idToPlayer.get(seatOrder.get(nextIndex)).getHand().isEmpty()
                && attempts <= seatOrder.size());
        currentPlayerId = seatOrder.get(nextIndex);
        log.info("Turn advanced to: {}", idToPlayer.get(currentPlayerId).getName());
    }

    public synchronized void registerPlay() {
        passCount = 0;
    }

    public synchronized void registerPass() {
        if (lastPlayedPlayerId == null) return;
        passCount++;
        if (passCount >= countActivePlayers() - 1) {
            roundWinnerId = lastPlayedPlayerId;

            int winnerTeam = idToPlayer.get(roundWinnerId).getTeam();
            addTeamScore(winnerTeam, tablePoints);
            checkScoreWinConditions(winnerTeam); // <-- this was missing

            table.clear();
            tablePoints = 0;
            passCount = 0;
            lastPlayedPlayerId = null;
            log.info("Round complete. Winner: {}", idToPlayer.get(roundWinnerId).getName());
        }
    }

    public synchronized void addTeamScore(int team, int points) {
        teamScores.merge(team, points, Integer::sum);
    }

    public synchronized Map<Integer, Integer> getTeamScores() {
        return Collections.unmodifiableMap(teamScores);
    }

    public synchronized UUID consumeRoundWinnerId() {
        UUID winner = roundWinnerId;
        roundWinnerId = null;
        return winner;
    }

    public synchronized void registerHandEmptied(UUID playerId) {
        if (gameOver || emptiedPlayers.contains(playerId)) return;

        emptiedPlayers.add(playerId);
        int team = idToPlayer.get(playerId).getTeam();

        if (firstEmptiedPlayerId == null) {
            firstEmptiedPlayerId = playerId;
            firstEmptiedTeam = team;

            // Condition 1 can fire right here: this team may already have
            // banked >= 90 points from earlier rounds, in which case they
            // win the instant "first emptier" status is established --
            // no need to wait for this round to finish.
            if (teamScores.get(team) >= 90) {
                gameOver = true;
                winningTeam = team;
                winReason = WinReason.FIRST_EMPTIER_90;
                return;
            }
        }

        // Condition 3: both players on this team have emptied their hands
        long teamEmptiedCount = emptiedPlayers.stream()
                .map(idToPlayer::get)
                .filter(p -> p.getTeam() == team)
                .count();

        if (teamEmptiedCount >= 2) {
            gameOver = true;
            winningTeam = team;
            winReason = WinReason.DOUBLE_OUT;
        }
    }

    /** Call right after addTeamScore() awards a round's points to `scoringTeam`. */
    public synchronized void checkScoreWinConditions(int scoringTeam) {
        if (gameOver) return;

        if (teamScores.get(scoringTeam) >= 140) {
            gameOver = true;
            winningTeam = scoringTeam;
            winReason = WinReason.SCORE_140;
            return;
        }

        if (firstEmptiedTeam != null
                && scoringTeam == firstEmptiedTeam
                && teamScores.get(scoringTeam) >= 90) {
            gameOver = true;
            winningTeam = scoringTeam;
            winReason = WinReason.FIRST_EMPTIER_90;
        }
    }

    private int countActivePlayers() {
        return seatOrder.size() - emptiedPlayers.size();
    }

    public synchronized void resetGame() {
        gameStarted = false;
        idToPlayer.clear();
        sessionToId.clear();
        table.clear();
        tablePoints = 0;
        seatOrder.clear();
        lastPlayedPlayerId = null;
        emptiedPlayers.clear();
        firstEmptiedPlayerId = null;
        firstEmptiedTeam = null;
        passCount = 0;
        roundWinnerId = null;
        teamScores.clear();
    }
}