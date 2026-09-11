package dev.workstho.bruhaha.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Discard / play stack.
 * Pattern adapted from Makao {@code Stack} / {@code DeckManager} recycling.
 */
public class CardPile {
    private final List<Card> cards = new ArrayList<>();

    public void push(Card card) {
        cards.add(card);
    }

    public void pushAll(List<Card> more) {
        cards.addAll(more);
    }

    public Card peek() {
        if (cards.isEmpty()) return null;
        return cards.get(cards.size() - 1);
    }

    public Card pop() {
        if (cards.isEmpty()) return null;
        return cards.remove(cards.size() - 1);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    public List<Card> drainAll() {
        List<Card> all = new ArrayList<>(cards);
        cards.clear();
        return all;
    }

    public List<Card> getCards() {
        return cards;
    }

    /** Recycle into a deck (Makao-style refresh). */
    public void recycleInto(Deck deck) {
        List<Card> recycled = drainAll();
        Collections.shuffle(recycled);
        deck.addAll(recycled);
    }
}
