package com.zhaojunan.paoyao_backend.model.entity;

import com.zhaojunan.paoyao_backend.model.enumeration.CardType;
import com.zhaojunan.paoyao_backend.model.enumeration.JokerType;
import com.zhaojunan.paoyao_backend.model.enumeration.Rank;
import com.zhaojunan.paoyao_backend.model.enumeration.Suit;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@Builder
@EqualsAndHashCode
public class Card {

    private CardType type;
    private Suit suit;
    private Rank rank;
    private JokerType jokerType;
    private int point;

    @Override
    public String toString() {
        if (type == CardType.JOKER) {
            return jokerType.getFileName();
        }
        return rank.getValue() + "_of_" + suit.getValue();
    }

    public static Card fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Card string is empty");
        }

        // Joker case
        if (value.equals("red_joker") || value.equals("black_joker")) {
            return Card.builder()
                    .type(CardType.JOKER)
                    .jokerType(JokerType.fromFileName(value))
                    .point(0)
                    .build();
        }

        // Normal card: "4_of_clubs"
        String[] parts = value.split("_of_");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid card format: " + value);
        }

        Rank rank = Rank.fromValue(parts[0]);
        Suit suit = Suit.fromValue(parts[1]);

        int point = switch (rank) {
            case FIVE -> 5;
            case TEN, K -> 10;
            default -> 0;
        };

        return Card.builder()
                .type(CardType.STANDARD)
                .rank(rank)
                .suit(suit)
                .point(point)
                .build();
    }

}
