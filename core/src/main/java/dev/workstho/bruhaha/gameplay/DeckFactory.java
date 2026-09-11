package dev.workstho.bruhaha.gameplay;

import dev.workstho.bruhaha.model.Card;
import dev.workstho.bruhaha.model.CardType;
import dev.workstho.bruhaha.model.Deck;

/**
 * Builds the Bruhaha deck composition.
 * Pattern adapted from Makao {@code CardFactory} / {@code DeckManager}.
 */
public final class DeckFactory {
    private DeckFactory() {}

    public static Deck createStandardDeck() {
        Deck deck = new Deck();
        int id = 0;
        id = add(deck, CardType.HIT, 28, id);
        id = add(deck, CardType.DODGE, 14, id);
        id = add(deck, CardType.DENIED, 10, id);
        id = add(deck, CardType.TRANSFER, 6, id);
        id = add(deck, CardType.ASSASSIN, 4, id);
        id = add(deck, CardType.RAMPAGE, 4, id);
        add(deck, CardType.HEAL, 4, id);
        deck.shuffle();
        return deck;
    }

    private static int add(Deck deck, CardType type, int count, int startId) {
        int id = startId;
        for (int i = 0; i < count; i++) {
            deck.add(new Card(type, id++));
        }
        return id;
    }
}
