package com.zhaojunan.paoyao_backend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhaojunan.paoyao_backend.game.GameManager;
import com.zhaojunan.paoyao_backend.game.GameRoom;
import com.zhaojunan.paoyao_backend.mapper.PlayerMapper;
import com.zhaojunan.paoyao_backend.model.dto.request.JoinRequest;
import com.zhaojunan.paoyao_backend.model.dto.response.ErrorResponse;
import com.zhaojunan.paoyao_backend.model.dto.response.GameStartResponse;
import com.zhaojunan.paoyao_backend.model.dto.response.JoinedResponse;
import com.zhaojunan.paoyao_backend.model.dto.response.PlayerListResponse;
import com.zhaojunan.paoyao_backend.model.entity.Player;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

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

        Player player = new Player(UUID.randomUUID(), req.getName(), session);

        boolean added = gameRoom.addPlayer(player);
        if (!added) {
            sendError(session, "Room is full or game already started");
            return;
        }

        // Track mapping
        sessionPlayerMap.put(session.getId(), player);

        // Send joined response ONLY to this player
        JoinedResponse joined = new JoinedResponse(
                "joined",
                player.getId().toString(),
                player.getName()
        );
        session.sendMessage(new TextMessage(mapper.writeValueAsString(joined)));

        // Broadcast updated player list
        broadcastPlayerList();

        // Auto-start when 4 players join
        if (gameRoom.isRoomFull()) {
            gameRoom.startGame();
            broadcastGameStart();
        }
    }

    private void broadcastPlayerList() throws Exception {
        PlayerListResponse list = new PlayerListResponse(
                "playerList",
                gameRoom.getPlayers().stream().map(PlayerMapper.INSTANCE::toDTO).toList()
        );

        broadcast(list);
    }

    private void broadcastGameStart() throws Exception {
        GameStartResponse res = new GameStartResponse("game_start",
                gameRoom.getPlayers().stream().map(PlayerMapper.INSTANCE::toDTO).toList());
        broadcast(res);
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
            ErrorResponse err = new ErrorResponse("error", msg);
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
