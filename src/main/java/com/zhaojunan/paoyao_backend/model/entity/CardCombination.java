package com.zhaojunan.paoyao_backend.model.entity;

import com.zhaojunan.paoyao_backend.model.enumeration.CombinationType;
import lombok.Getter;

import java.util.List;

@Getter
public class CardCombination {

    private final CombinationType type;
    private final List<Card> cards;

    // For SINGLE/PAIR/SINGLE_STRAIGHT/DOUBLE_STRAIGHT: the top card's rank position
    //   (using the single-rank order 4<5<...<3<BlackJoker<RedJoker), used to compare
    //   two combinations of the same type and length.
    // For BOMB/JOKER_BOMB/ROCKET: the computed tier (as worked out in our rules
    //   conversation — count for normal bombs, rounded joker-point-sum, or 2n for rockets).
    private final int strength;

    // Card count. Needed for straight-length matching (must be equal to follow),
    // and to distinguish e.g. a 3-card bomb from a 4-card bomb before tier comparison.
    private final int length;

    public CardCombination(CombinationType type, List<Card> cards, int strength, int length) {
        this.type = type;
        this.cards = cards;
        this.strength = strength;
        this.length = length;
    }
}
