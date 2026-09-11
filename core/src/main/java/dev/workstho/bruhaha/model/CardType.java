package dev.workstho.bruhaha.model;

import com.badlogic.gdx.graphics.Color;

/** Bruhaha card kinds (custom set, not French-suited). */
public enum CardType {
    HIT("HIT", Category.ATTACK, new Color(0.92f, 0.18f, 0.22f, 1f), 1, "cards/hit.png"),
    DODGE("DODGE", Category.REACTION, new Color(0.45f, 0.85f, 0.20f, 1f), 0, "cards/dodge.png"),
    DENIED("DENIED", Category.REACTION, new Color(0.25f, 0.72f, 0.95f, 1f), 0, "cards/denied.png"),
    RAMPAGE("RAMPAGE", Category.POWER, new Color(0.55f, 0.08f, 0.18f, 1f), 2, "cards/rampage.png"),
    HEAL("HEAL", Category.POWER, new Color(0.98f, 0.82f, 0.12f, 1f), 0, "cards/heal.png"),
    ASSASSIN("ASSASSIN", Category.SPECIAL, new Color(0.08f, 0.08f, 0.10f, 1f), 2, "cards/assassin.png"),
    TRANSFER("TRANSFER", Category.SPECIAL, new Color(0.55f, 0.22f, 0.72f, 1f), 1, "cards/transfer.png");

    public enum Category { ATTACK, REACTION, POWER, SPECIAL }

    public final String label;
    public final Category category;
    public final Color banner;
    public final int power;
    public final String texturePath;

    CardType(String label, Category category, Color banner, int power, String texturePath) {
        this.label = label;
        this.category = category;
        this.banner = banner;
        this.power = power;
        this.texturePath = texturePath;
    }

    public boolean isReaction() {
        return category == Category.REACTION;
    }

    public boolean isPlayableOnTurn() {
        return category == Category.ATTACK || category == Category.POWER || category == Category.SPECIAL;
    }
}
