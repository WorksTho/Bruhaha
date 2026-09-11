package dev.workstho.bruhaha.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Player hand.
 * Adapted from jCards {@code io.lyuda.jcards.Hand} — MIT License (lyudaio).
 */
public class Hand {
    private final List<Card> cards = new ArrayList<>();

    public void addCard(Card card) {
        cards.add(Objects.requireNonNull(card, "card"));
    }

    public void addAll(List<Card> more) {
        for (Card card : more) addCard(card);
    }

    public boolean removeCard(Card card) {
        return cards.remove(Objects.requireNonNull(card, "card"));
    }

    public Card removeAt(int index) {
        return cards.remove(index);
    }

    public Card get(int index) {
        return cards.get(index);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public List<Card> getCards() {
        return cards;
    }

    public void sort() {
        Collections.sort(cards);
    }

    public int count(CardType type) {
        int n = 0;
        for (Card c : cards) if (c.getType() == type) n++;
        return n;
    }

    public int indexOf(CardType type) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getType() == type) return i;
        }
        return -1;
    }

    public Optional<Card> findFirst(CardType type) {
        for (Card c : cards) if (c.getType() == type) return Optional.of(c);
        return Optional.empty();
    }

    public boolean hasThreeOfKind() {
        for (CardType t : CardType.values()) {
            if (count(t) >= 3) return true;
        }
        return false;
    }

    public boolean hasThree(CardType type) {
        return count(type) >= 3;
    }

    /** Removes up to {@code amount} cards of {@code type}; returns how many were removed. */
    public List<Card> takeMatching(CardType type, int amount) {
        List<Card> taken = new ArrayList<>();
        for (int i = cards.size() - 1; i >= 0 && taken.size() < amount; i--) {
            if (cards.get(i).getType() == type) taken.add(cards.remove(i));
        }
        return taken;
    }

    @Override
    public String toString() {
        return "Hand" + cards;
    }
}
