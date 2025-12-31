package com.zhaojunan.paoyao_backend.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebSocketMessage<T> {

    private String type;   // "your_hand", "play_cards", etc.
    private T payload;

}
