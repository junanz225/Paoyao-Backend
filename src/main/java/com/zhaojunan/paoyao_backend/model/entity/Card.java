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

}
