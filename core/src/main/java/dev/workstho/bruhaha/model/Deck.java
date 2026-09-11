package dev.workstho.bruhaha.model;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Draw deck with shuffle / deal.
 * Adapted from jCards {@code io.lyuda.jcards.Deck} — MIT License (lyudaio).
 */
public class Deck {
    private final List<Card> cards = new ArrayList<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public void add(Card card) {
        cards.add(card);
    }

    public void addAll(List<Card> more) {
        cards.addAll(more);
    }

    public void shuffle() {
        long seed = secureRandom.nextLong();
        Collections.shuffle(cards, new Random(seed));
    }

    public Card deal() {
        if (cards.isEmpty()) throw new IllegalStateException("Deck is empty");
        return cards.remove(0);
    }

    public List<Card> deal(int amount) {
        if (amount > cards.size()) {
            throw new IllegalStateException("Cannot deal " + amount + " from " + cards.size());
        }
        List<Card> dealt = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) dealt.add(deal());
        return dealt;
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    public List<Card> getCards() {
        return cards;
    }

    public void clear() {
        cards.clear();
    }
}
