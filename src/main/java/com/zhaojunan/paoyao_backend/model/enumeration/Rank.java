package com.zhaojunan.paoyao_backend.model.enumeration;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Rank {

    A("ace"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    TEN("10"),
    J("jack"),
    Q("queen"),
    K("king");

    private final String value;

    Rank(String value) {
        this.value = value;
    }

    public static Rank fromValue(String value) {
        return Arrays.stream(values())
                .filter(r -> r.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid rank: " + value));
    }

}
