package dev.workstho.bruhaha.model;

/**
 * Card-game player with a hand and hit points.
 * Adapted from jCards {@code io.lyuda.jcards.game.Player} — MIT License (lyudaio).
 */
public class Player {
    private final String name;
    private final boolean human;
    private final Hand hand = new Hand();
    private int hp;
    private final int maxHp;

    public Player(String name, boolean human, int maxHp) {
        this.name = name;
        this.human = human;
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    public String getName() {
        return name;
    }

    public boolean isHuman() {
        return human;
    }

    public Hand getHand() {
        return hand;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(maxHp, hp));
    }

    public void heal(int amount) {
        setHp(hp + amount);
    }

    public void damage(int amount) {
        setHp(hp - amount);
    }

    public boolean isEliminated() {
        return hp <= 0;
    }

    public void addCard(Card card) {
        hand.addCard(card);
    }

    public void removeCard(Card card) {
        hand.removeCard(card);
    }
}
