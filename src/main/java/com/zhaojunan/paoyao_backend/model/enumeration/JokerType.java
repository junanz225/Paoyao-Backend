package com.zhaojunan.paoyao_backend.model.enumeration;

import lombok.Getter;
import java.util.Arrays;

@Getter
public enum JokerType {

    RED("red_joker"),
    BLACK("black_joker");

    private final String fileName;

    JokerType(String fileName) {
        this.fileName = fileName;
    }

    public static JokerType fromFileName(String fileName) {
        return Arrays.stream(values())
                .filter(j -> j.getFileName().equals(fileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid joker: " + fileName));
    }

}
