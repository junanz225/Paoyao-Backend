package com.zhaojunan.paoyao_backend.entity;

import lombok.Data;

@Data
public class Message {
    private String type;   // e.g. "join", "play", "chat"
    private String player; // player name
    private Object data;   // can hold cards, text, etc.
}
