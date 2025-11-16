package com.zhaojunan.paoyao_backend.game;

import org.springframework.stereotype.Component;

@Component
public class GameManager {

    private final GameRoom gameRoom = new GameRoom();

    public GameRoom getRoom() {
        return gameRoom;
    }

}
