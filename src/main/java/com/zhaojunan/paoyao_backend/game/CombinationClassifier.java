package com.zhaojunan.paoyao_backend.game;

import com.zhaojunan.paoyao_backend.model.entity.Card;
import com.zhaojunan.paoyao_backend.model.entity.CardCombination;
import com.zhaojunan.paoyao_backend.model.enumeration.CardType;
import com.zhaojunan.paoyao_backend.model.enumeration.CombinationType;
import com.zhaojunan.paoyao_backend.model.enumeration.JokerType;
import com.zhaojunan.paoyao_backend.model.enumeration.Rank;

import java.util.List;

public class CombinationClassifier {

    private static final int MIN_STRAIGHT_RANKS = 3;
    // Straights only run 4..A; 2, 3 and jokers all have strength above A
    private static final int MAX_STRAIGHT_STRENGTH = Rank.A.getStrength();
    private static final int MIN_BOMB_SIZE = 3;
    private static final int MAX_BOMB_SIZE = 8; // two decks = eight copies of a rank
    private static final int MIN_JOKER_BOMB_SIZE = 2;
    private static final int MAX_JOKER_BOMB_SIZE = 4; // two decks = four jokers
    private static final int BLACK_JOKER_HALF_POINTS = 3; // 1.5 points
    private static final int RED_JOKER_HALF_POINTS = 5;   // 2.5 points

    public static CardCombination classify(List<Card> cards) {
        if (cards == null) {
            throw new IllegalArgumentException("cards list must not be null");
        }
        if (cards.isEmpty()) {
            return null;
        }

        if (cards.size() == 1) {
            return new CardCombination(CombinationType.SINGLE, cards, cards.get(0).getStrength(), 1);
        }

        if (cards.size() == 2) {
            Card first = cards.get(0);
            Card second = cards.get(1);

            // Two jokers together are NOT a pair — that's a joker bomb (Step 5)
            boolean eitherIsJoker = first.getType() == CardType.JOKER || second.getType() == CardType.JOKER;

            if (!eitherIsJoker && first.getRank() == second.getRank()) {
                return new CardCombination(CombinationType.PAIR, cards, first.getStrength(), 2);
            }
        }

        CardCombination bomb = classifyNormalBomb(cards);
        if (bomb != null) {
            return bomb;
        }
        CardCombination jokerBomb = classifyJokerBomb(cards);
        if (jokerBomb != null) {
            return jokerBomb;
        }

        return classifyStraight(cards); // returns null if not a straight either
    }

    /**
     * Recognizes single-straights (e.g. 456) and double-straights (e.g. 445566).
     * Rules: at least 3 consecutive ranks, within 4..A, every rank appearing the
     * same number of times (once = single-straight, twice = double-straight).
     * Strength = the top rank's strength, so 456 < 567.
     */
    private static CardCombination classifyStraight(List<Card> cards) {
        int[] counts = new int[MAX_STRAIGHT_STRENGTH + 1];
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (Card card : cards) {
            int s = card.getStrength();
            if (s > MAX_STRAIGHT_STRENGTH) {
                return null; // a 2, a 3, or a joker — none can be in a straight
            }
            counts[s]++;
            min = Math.min(min, s);
            max = Math.max(max, s);
        }

        int ranks = max - min + 1;
        if (ranks < MIN_STRAIGHT_RANKS || cards.size() % ranks != 0) {
            return null;
        }

        int copies = cards.size() / ranks;
        if (copies != 1 && copies != 2) {
            return null;
        }

        // Every rank from min to max must appear exactly `copies` times (no gaps, no extras)
        for (int s = min; s <= max; s++) {
            if (counts[s] != copies) {
                return null;
            }
        }

        CombinationType type = copies == 1
                ? CombinationType.SINGLE_STRAIGHT
                : CombinationType.DOUBLE_STRAIGHT;

        return new CardCombination(type, cards, max, cards.size());
    }

    /**
     * Normal bomb: 3 to 8 cards of the same rank (jokers excluded; joker bombs
     * and rockets are recognized in Step 5). Tier = card count, and the rank
     * orders bombs within the same tier (444 < 555 < ... < 333).
     */
    private static CardCombination classifyNormalBomb(List<Card> cards) {
        int size = cards.size();
        if (size < MIN_BOMB_SIZE || size > MAX_BOMB_SIZE) {
            return null;
        }

        Rank rank = cards.get(0).getRank(); // null for a joker
        if (rank == null) {
            return null;
        }
        for (Card card : cards) {
            if (card.getRank() != rank) {
                return null; // mixed ranks, or a joker in the mix
            }
        }

        int strength = size * CardCombination.BOMB_TIER_SCALE + rank.getStrength();
        return new CardCombination(CombinationType.BOMB, cards, strength, size);
    }

    /**
     * Joker bomb: 2 to 4 cards, all jokers. Points are summed (Black 1.5, Red 2.5)
     * and rounded up; that number is the tier the bomb is the greatest of.
     */
    private static CardCombination classifyJokerBomb(List<Card> cards) {
        int size = cards.size();
        if (size < MIN_JOKER_BOMB_SIZE || size > MAX_JOKER_BOMB_SIZE) {
            return null;
        }

        int halfPoints = 0;
        for (Card card : cards) {
            if (card.getType() != CardType.JOKER) {
                return null;
            }
            halfPoints += card.getJokerType() == JokerType.BLACK
                    ? BLACK_JOKER_HALF_POINTS
                    : RED_JOKER_HALF_POINTS;
        }

        int tier = (halfPoints + 1) / 2; // integer division rounds .5 up
        int strength = tier * CardCombination.BOMB_TIER_SCALE + CardCombination.JOKER_BOMB_WITHIN_TIER;
        return new CardCombination(CombinationType.JOKER_BOMB, cards, strength, size);
    }
}