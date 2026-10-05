package com.zhaojunan.paoyao_backend.model.enumeration;

import lombok.Getter;
import java.util.Arrays;

@Getter
public enum JokerType {

    BLACK("black_joker", 13),
    RED("red_joker", 14);

    private final String fileName;
    private final int strength;

    JokerType(String fileName, int strength) {
        this.fileName = fileName;
        this.strength = strength;
    }

    public static JokerType fromFileName(String fileName) {
        return Arrays.stream(values())
                .filter(j -> j.getFileName().equals(fileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid joker: " + fileName));
    }

}