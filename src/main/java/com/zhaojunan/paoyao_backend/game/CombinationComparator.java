package com.zhaojunan.paoyao_backend.game;


import com.zhaojunan.paoyao_backend.model.entity.CardCombination;
import com.zhaojunan.paoyao_backend.model.enumeration.CombinationType;

import static com.zhaojunan.paoyao_backend.model.enumeration.CombinationType.DOUBLE_STRAIGHT;

public class CombinationComparator {

    // A double-straight can only be bombed by a tier-4 bomb or higher (4444+),
    // so a plain triple (tier 3) is not enough.
    private static final int MIN_BOMB_TIER_VS_DOUBLE_STRAIGHT = 4;

    /**
     * Can `play` legally be played on top of `table`?
     * A null `table` means the player is leading the round: any recognized play is legal.
     * A null `play` (unrecognized shape) never beats anything.
     */
    public static boolean beats(CardCombination table, CardCombination play) {
        if (play == null) {
            return false;
        }
        if (table == null) {
            return true;
        }

        // Bombs are universal: they can go on top of any shape,
        // and are compared on the single bomb ladder when the table is also a bomb.
        if (play.isBomb()) {
            if (table.isBomb()) {
                return play.getStrength() > table.getStrength();
            }
            if (table.getType() == DOUBLE_STRAIGHT) {
                return play.getBombTier() >= MIN_BOMB_TIER_VS_DOUBLE_STRAIGHT;
            }
            return true;
        }

        // A normal shape can never follow a bomb
        if (table.isBomb()) {
            return false;
        }

        return switch (table.getType()) {
            case SINGLE, PAIR ->
                    play.getType() == table.getType()
                            && play.getStrength() > table.getStrength();

            // Single-straight accepts a single- OR double-straight of the same rank span
            case SINGLE_STRAIGHT ->
                    (play.getType() == CombinationType.SINGLE_STRAIGHT
                            || play.getType() == DOUBLE_STRAIGHT)
                            && play.getRankSpan() == table.getRankSpan()
                            && play.getStrength() > table.getStrength();

            // Double-straight only accepts a double-straight of the same rank span
            case DOUBLE_STRAIGHT ->
                    play.getType() == DOUBLE_STRAIGHT
                            && play.getRankSpan() == table.getRankSpan()
                            && play.getStrength() > table.getStrength();

            default -> false;
        };
    }
}