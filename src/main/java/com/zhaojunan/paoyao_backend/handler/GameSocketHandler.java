package com.zhaojunan.paoyao_backend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhaojunan.paoyao_backend.game.GameManager;
import com.zhaojunan.paoyao_backend.game.GameRoom;
import com.zhaojunan.paoyao_backend.mapper.PlayerMapper;
import com.zhaojunan.paoyao_backend.model.dto.request.JoinRequest;
import com.zhaojunan.paoyao_backend.model.dto.response.DealCardsPayload;
import com.zhaojunan.paoyao_backend.model.dto.response.PlayerDTO;
import com.zhaojunan.paoyao_backend.model.dto.response.WebSocketMessage;
import com.zhaojunan.paoyao_backend.model.entity.Player;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final GameRoom gameRoom;
    private final Map<String, Player> sessionPlayerMap = new ConcurrentHashMap<>();

    public GameSocketHandler(GameManager gameManager) {
        this.gameRoom = gameManager.getRoom();
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

            switch (type) {
                case "join":
                    handleJoin(session, json);
                    break;

                default:
                    sendError(session, "Unknown message type: " + type);
            }

        } catch (Exception e) {
            sendError(session, "Invalid message format: " + e.getMessage());
        }
    }

    private void handleJoin(WebSocketSession session, Map<String, Object> json) throws Exception {
        // Convert JSON to DTO
        JoinRequest req = mapper.convertValue(json, JoinRequest.class);

        Player player = Player.builder()
                .id(UUID.randomUUID())
                .name(req.getName())
                .session(session)
                .build();

        boolean added = gameRoom.addPlayer(player);
        if (!added) {
            sendError(session, "Room is full or game already started");
            return;
        }

        // Track mapping
        sessionPlayerMap.put(session.getId(), player);

        // Send joined response ONLY to this player
        PlayerDTO playerDto = PlayerDTO.builder()
                .playerId(player.getId().toString())
                .playerName(player.getName())
                .build();
        WebSocketMessage<PlayerDTO> joined = WebSocketMessage.<PlayerDTO>builder()
                .type("joined")
                .payload(playerDto)
                .build();

        session.sendMessage(new TextMessage(mapper.writeValueAsString(joined)));

        // Broadcast updated player list
        broadcastPlayerList();

        // Auto-start when 4 players join
        if (gameRoom.isRoomFull()) {
            gameRoom.startGame();
            broadcastGameStart();
            sendDealCards();
        }
    }

    private void broadcastPlayerList() throws Exception {
        List<PlayerDTO> playerDtos = gameRoom.getPlayers().stream().map(PlayerMapper.INSTANCE::toDTO).toList();

        WebSocketMessage<List<PlayerDTO>> playerList = WebSocketMessage.<List<PlayerDTO>>builder()
                .type("player_list")
                .payload(playerDtos)
                .build();

        broadcast(playerList);
    }

    private void broadcastGameStart() throws Exception {
        List<PlayerDTO> playerDtos = gameRoom.getPlayers().stream().map(PlayerMapper.INSTANCE::toDTO).toList();

        WebSocketMessage<List<PlayerDTO>> playerList = WebSocketMessage.<List<PlayerDTO>>builder()
                .type("game_start")
                .payload(playerDtos)
                .build();

        broadcast(playerList);
    }

    private void sendDealCards() throws Exception {
        for (Player player : gameRoom.getPlayers()) {

            DealCardsPayload payload = DealCardsPayload.builder()
                    .playerId(player.getId().toString())
                    .cards(player.getHand())
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

        for (Player p : gameRoom.getPlayers()) {
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
        Player p = sessionPlayerMap.remove(session.getId());
        if (p != null) {
            gameRoom.removePlayer(p);
        }

        try {
            broadcastPlayerList();
        } catch (Exception ignored) {
        }
    }

}
