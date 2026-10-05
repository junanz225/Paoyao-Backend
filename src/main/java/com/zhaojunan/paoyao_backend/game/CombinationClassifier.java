package com.zhaojunan.paoyao_backend.game;

import com.zhaojunan.paoyao_backend.model.entity.Card;
import com.zhaojunan.paoyao_backend.model.entity.CardCombination;
import com.zhaojunan.paoyao_backend.model.enumeration.CardType;
import com.zhaojunan.paoyao_backend.model.enumeration.CombinationType;

import java.util.List;

public class CombinationClassifier {

    /**
     * Classifies a play as a Single or Pair. Returns null if the cards don't form
     * either shape. Straights, bombs, joker bombs, and rockets are recognized by
     * later steps, added into this same method.
     */
    public static CardCombination classify(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return null;
        }

        if (cards.size() == 1) {
            return new CardCombination(CombinationType.SINGLE, cards, cards.get(0).getStrength(), 1);
        }

        if (cards.size() == 2) {
            Card first = cards.get(0);
            Card second = cards.get(1);

            boolean eitherIsJoker = first.getType() == CardType.JOKER || second.getType() == CardType.JOKER;

            if (!eitherIsJoker && first.getRank() == second.getRank()) {
                return new CardCombination(CombinationType.PAIR, cards, first.getStrength(), 2);
            }
        }

        return null;
    }

}