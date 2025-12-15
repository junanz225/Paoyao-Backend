package com.zhaojunan.paoyao_backend.model.enumeration;

import lombok.Getter;

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

}
