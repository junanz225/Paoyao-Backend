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
    // For BOMB/JOKER_BOMB/ROCKET: tier * BOMB_TIER_SCALE + withinTier.
    private final int strength;

    // Card count. Needed for straight-length matching (must be equal to follow),
    // and to distinguish e.g. a 3-card bomb from a 4-card bomb before tier comparison.
    private final int length;
    public static final int BOMB_TIER_SCALE = 100;
    // Within-tier values for bombs. Normal bombs use their rank strength (0..12),
    // so these sit above every normal bomb of the same tier.
    public static final int JOKER_BOMB_WITHIN_TIER = 13;
    public static final int ROCKET_WITHIN_TIER = 14;

    public CardCombination(CombinationType type, List<Card> cards, int strength, int length) {
        this.type = type;
        this.cards = cards;
        this.strength = strength;
        this.length = length;
    }

    // Only meaningful for BOMB / JOKER_BOMB / ROCKET
    public int getBombTier() {
        return strength / BOMB_TIER_SCALE;
    }

    public boolean isBomb() {
        return type == CombinationType.BOMB
                || type == CombinationType.JOKER_BOMB
                || type == CombinationType.ROCKET;
    }

    // Number of distinct ranks a straight spans (456 = 3, 445566 = 3).
// Only meaningful for SINGLE_STRAIGHT / DOUBLE_STRAIGHT.
    public int getRankSpan() {
        return type == CombinationType.DOUBLE_STRAIGHT ? length / 2 : length;
    }

}
