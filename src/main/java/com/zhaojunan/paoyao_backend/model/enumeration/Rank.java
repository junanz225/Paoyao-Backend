package com.zhaojunan.paoyao_backend.model.enumeration;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Rank {

    FOUR("4", 0),
    FIVE("5", 1),
    SIX("6", 2),
    SEVEN("7", 3),
    EIGHT("8", 4),
    NINE("9", 5),
    TEN("10", 6),
    J("jack", 7),
    Q("queen", 8),
    K("king", 9),
    A("ace", 10),
    TWO("2", 11),
    THREE("3", 12);

    private final String value;
    private final int strength;

    Rank(String value, int strength) {
        this.value = value;
        this.strength = strength;
    }

    public static Rank fromValue(String value) {
        return Arrays.stream(values())
                .filter(r -> r.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid rank: " + value));
    }

}
