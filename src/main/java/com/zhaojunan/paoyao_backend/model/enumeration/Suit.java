package com.zhaojunan.paoyao_backend.model.enumeration;

import lombok.Getter;
import java.util.Arrays;

@Getter
public enum Suit {

    HEART("hearts"),
    DIAMOND("diamonds"),
    CLUB("clubs"),
    SPADE("spades");

    private final String value;

    Suit(String value) {
        this.value = value;
    }

    public static Suit fromValue(String value) {
        return Arrays.stream(values())
                .filter(s -> s.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid suit: " + value));
    }

}
