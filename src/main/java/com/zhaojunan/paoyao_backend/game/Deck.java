package com.zhaojunan.paoyao_backend.game;

import com.zhaojunan.paoyao_backend.model.entity.Card;
import com.zhaojunan.paoyao_backend.model.enumeration.CardType;
import com.zhaojunan.paoyao_backend.model.enumeration.JokerType;
import com.zhaojunan.paoyao_backend.model.enumeration.Rank;
import com.zhaojunan.paoyao_backend.model.enumeration.Suit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private final List<Card> cards = new ArrayList<>();

    public Deck() {
        // Build 2 full decks
        for ( int d = 0; d < 2; d++) {

            // Standard cards
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {

                    int point = 0;
                    if (rank == Rank.FIVE) point = 5;
                    else if (rank == Rank.TEN) point = 10;
                    else if (rank == Rank.K) point = 10;

                    cards.add(
                            Card.builder()
                                    .type(CardType.STANDARD)
                                    .suit(suit)
                                    .rank(rank)
                                    .point(point)
                                    .build()
                    );
                }
            }

            // Jokers
            cards.add(
                    Card.builder()
                            .type(CardType.JOKER)
                            .jokerType(JokerType.SMALL)
                            .point(0)
                            .build()
            );

            cards.add(
                    Card.builder()
                            .type(CardType.JOKER)
                            .jokerType(JokerType.BIG)
                            .point(0)
                            .build()
            );
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<Card> deal(int count) {
        List<Card> hand = new ArrayList<>(cards.subList(0, count));
        cards.subList(0, count).clear();
        return hand;
    }

}
