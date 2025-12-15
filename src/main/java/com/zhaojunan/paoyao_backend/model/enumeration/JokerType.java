package com.zhaojunan.paoyao_backend.model.enumeration;

import lombok.Getter;

@Getter
public enum JokerType {

    RED("red_joker"),
    BLACK("black_joker");

    private final String fileName;

    JokerType(String fileName) {
        this.fileName = fileName;
    }

}
