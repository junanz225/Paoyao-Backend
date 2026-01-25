package com.zhaojunan.paoyao_backend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhaojunan.paoyao_backend.game.GameManager;
import com.zhaojunan.paoyao_backend.mapper.PlayerMapper;
import com.zhaojunan.paoyao_backend.model.dto.request.JoinRequest;
import com.zhaojunan.paoyao_backend.model.dto.request.PlayCardRequest;
import com.zhaojunan.paoyao_backend.model.dto.response.DealCardsPayload;
import com.zhaojunan.paoyao_backend.model.dto.response.GameStatePayload;
import com.zhaojunan.paoyao_backend.model.dto.response.PlayerDTO;
import com.zhaojunan.paoyao_backend.model.dto.response.PlayerStateDTO;
import com.zhaojunan.paoyao_backend.model.dto.WebSocketMessage;
import com.zhaojunan.paoyao_backend.model.dto.response.TableStateDTO;
import com.zhaojunan.paoyao_backend.model.entity.Player;
import com.zhaojunan.paoyao_backend.model.entity.Card;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class GameSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final PlayerMapper playerMapper;
    private final GameManager gameManager;

    public GameSocketHandler(GameManager gameManager, PlayerMapper playerMapper) {
        this.gameManager = gameManager;
        this.playerMapper = playerMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("New connection: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // Parse incoming JSON
            Map<String, Object> json = mapper.readValue(message.getPayload(), Map.class);
            String type = (String) json.get("type");
            if (type == null) {
                sendError(session, "Missing message type");
                return;
            }

            Object payloadObj = json.get("payload");
            Map<String, Object> payload =
                    payloadObj instanceof Map
                            ? (Map<String, Object>) payloadObj
                            : Map.of();

            switch (type) {
                case "join":
                    handleJoin(session, payload);
                    break;
                case "play_cards":
                    handlePlayCard(session, payload);
                    break;

                default:
                    sendError(session, "Unknown message type: " + type);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            sendError(session, "Invalid message format: " + e.getMessage());
        }
    }

    private void handlePlayCard(WebSocketSession session, Map<String, Object> json) throws Exception {
        // Convert JSON to DTO
        PlayCardRequest request = mapper.convertValue(json, PlayCardRequest.class);

        List<Card> playedCards = request.getPlayedCards().stream().map(Card::fromString).toList();
        gameManager.playCards(session, playedCards);

        broadcastGameState();
    }

    private void handleJoin(WebSocketSession session, Map<String, Object> json) throws Exception {
        // Convert JSON to DTO
        JoinRequest request = mapper.convertValue(json, JoinRequest.class);

        Player player = gameManager.join(session, request.getName());

        if (player == null) {
            sendError(session, "Room is full or game already started");
            return;
        }

        // Send joined response
        PlayerDTO playerDto = playerMapper.toDTO(player);
        WebSocketMessage<PlayerDTO> joined = WebSocketMessage.<PlayerDTO>builder()
                .type("joined")
                .payload(playerDto)
                .build();

        session.sendMessage(new TextMessage(mapper.writeValueAsString(joined)));
        broadcastPlayerList();

        if (gameManager.getRoom().isRoomFull()) {
            gameManager.getRoom().startGame();
            broadcastGameStart();
            sendDealCards();
            broadcastGameState();
        }
    }

    private void broadcastPlayerList() throws Exception {
        List<PlayerDTO> playerDtos = gameManager.getPlayers().stream().map(playerMapper::toDTO).toList();

        WebSocketMessage<List<PlayerDTO>> playerList = WebSocketMessage.<List<PlayerDTO>>builder()
                .type("player_list")
                .payload(playerDtos)
                .build();

        broadcast(playerList);
    }

    private void broadcastGameStart() throws Exception {
        List<PlayerDTO> playerDtos = gameManager.getPlayers().stream().map(playerMapper::toDTO).toList();

        WebSocketMessage<List<PlayerDTO>> playerList = WebSocketMessage.<List<PlayerDTO>>builder()
                .type("game_start")
                .payload(playerDtos)
                .build();

        broadcast(playerList);
    }

    private void broadcastGameState() throws Exception {
        List<PlayerStateDTO> playerStates = gameManager.getPlayers().stream()
                .map(player -> PlayerStateDTO.builder()
                        .playerId(player.getId().toString())
                        .playerName(player.getName())
                        .cardCount(player.getHandSize())
                        .build()
                )
                .toList();


        TableStateDTO tableState = TableStateDTO.builder()
                .lastPlayedPlayerId(
                        Objects.toString(
                                gameManager.getRoom().getLastPlayedPlayerId(),
                                null
                        )
                )
                .cards(
                        gameManager.getRoom()
                                .getTable()
                                .stream()
                                .map(Card::toString)
                                .toList()
                )
                .build();

        GameStatePayload gameStatePayload = GameStatePayload.builder()
                .playerStates(playerStates)
                .currentTurnPlayerId(null)
                .tableState(tableState)
                .tablePoints(gameManager.getRoom().getTablePoints())
                .build();

        WebSocketMessage<GameStatePayload> gameState = WebSocketMessage.<GameStatePayload>builder()
                .type("game_state")
                .payload(gameStatePayload)
                .build();

        broadcast(gameState);
    }

    private void sendDealCards() throws Exception {
        for (Player player : gameManager.getPlayers()) {

            List<String> cardKeys = player.getHand()
                    .stream()
                    .map(Card::toString)
                    .toList();

            DealCardsPayload payload = DealCardsPayload.builder()
                    .playerId(player.getId().toString())
                    .cards(cardKeys)
                    .build();

            WebSocketMessage<DealCardsPayload> msg =
                    WebSocketMessage.<DealCardsPayload>builder()
                            .type("deal_cards")
                            .payload(payload)
                            .build();

            player.getSession()
                    .sendMessage(new TextMessage(mapper.writeValueAsString(msg)));
        }
    }

    private void broadcast(Object messageObj) throws Exception {
        String json = mapper.writeValueAsString(messageObj);

        for (Player p : gameManager.getPlayers()) {
            if (p.getSession().isOpen()) {
                p.getSession().sendMessage(new TextMessage(json));
            }
        }
    }

    private void sendError(WebSocketSession session, String msg) {
        try {
            WebSocketMessage<String> err =  WebSocketMessage.<String>builder()
                    .type("error")
                    .payload(msg)
                    .build();
            session.sendMessage(new TextMessage(mapper.writeValueAsString(err)));
        } catch (Exception ignored) {}
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        gameManager.leave(session);

        try {
            broadcastPlayerList();
        } catch (Exception ignored) {
        }
    }

}
