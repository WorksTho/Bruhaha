package dev.workstho.bruhaha.model;

import java.util.Objects;

/**
 * Single Bruhaha card.
 * Structure inspired by jCards ({@code io.lyuda.jcards.Card}) — MIT.
 */
public class Card implements Comparable<Card> {
    private final CardType type;
    private final int id;

    public Card(CardType type, int id) {
        this.type = Objects.requireNonNull(type, "type");
        this.id = id;
    }

    public CardType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(Card other) {
        int byType = type.compareTo(other.type);
        return byType != 0 ? byType : Integer.compare(id, other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return id == card.id && type == card.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }

    @Override
    public String toString() {
        return type.label + "#" + id;
    }
}
