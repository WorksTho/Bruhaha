package dev.workstho.bruhaha.gameplay;

import com.badlogic.gdx.utils.Array;
import dev.workstho.bruhaha.model.Card;
import dev.workstho.bruhaha.model.CardPile;
import dev.workstho.bruhaha.model.CardType;
import dev.workstho.bruhaha.model.Deck;
import dev.workstho.bruhaha.model.Hand;
import dev.workstho.bruhaha.model.Player;

import java.util.List;

/**
 * Core Bruhaha rules / session state (Makao-style backend, separated from rendering).
 */
public class GameSession {
    public static final int MAX_HP = 5;
    public static final int MIN_HAND = 5;
    public static final int START_HAND = 5;
    public static final float REACTION_SECONDS = 4.5f;

    public enum Phase {
        HUMAN_TURN,
        BOT_TURN,
        AWAITING_REACTION,
        GAME_OVER
    }

    public interface Listener {
        void onMessage(String text);
        void onCardPlayed(CardType type);
        void onStateChanged();
        void onGameOver(Player winner);
    }

    private final Player human;
    private final Player bot;
    private Deck deck;
    private final CardPile discard = new CardPile();
    private Phase phase = Phase.HUMAN_TURN;
    private Player current;
    private Player winner;

    private boolean awaitingReaction;
    private Player attackSource;
    private Player attackTarget;
    private CardType attackType;
    private int pendingDamage;
    private float reactionTimer;

    private CardType lastPlayed;
    private Listener listener = new Listener() {
        @Override public void onMessage(String text) {}
        @Override public void onCardPlayed(CardType type) {}
        @Override public void onStateChanged() {}
        @Override public void onGameOver(Player winner) {}
    };

    public GameSession() {
        human = new Player("YOU", true, MAX_HP);
        bot = new Player("BOT", false, MAX_HP);
    }

    public void setListener(Listener listener) {
        this.listener = listener != null ? listener : this.listener;
    }

    public void startNewRound() {
        human.getHand().getCards().clear();
        bot.getHand().getCards().clear();
        human.setHp(MAX_HP);
        bot.setHp(MAX_HP);
        discard.drainAll();
        deck = DeckFactory.createStandardDeck();
        deal(human, START_HAND);
        deal(bot, START_HAND);
        current = human;
        phase = Phase.HUMAN_TURN;
        winner = null;
        awaitingReaction = false;
        lastPlayed = null;
        message("Eliminate the bot. Shout BRUHAHA!");
        listener.onStateChanged();
    }

    private void deal(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Card c = drawOne();
            if (c != null) player.addCard(c);
        }
    }

    private Card drawOne() {
        if (deck.isEmpty()) {
            if (discard.isEmpty()) return null;
            discard.recycleInto(deck);
            deck.shuffle();
        }
        if (deck.isEmpty()) return null;
        return deck.deal();
    }

    public void refill(Player player) {
        while (player.getHand().size() < MIN_HAND) {
            Card c = drawOne();
            if (c == null) break;
            player.addCard(c);
        }
        listener.onStateChanged();
    }

    public boolean tryPlay(Player player, int handIndex) {
        if (phase == Phase.GAME_OVER) return false;
        if (awaitingReaction) return false;
        if (player != current) return false;
        Hand hand = player.getHand();
        if (handIndex < 0 || handIndex >= hand.size()) return false;

        Card card = hand.get(handIndex);
        if (card.getType().isReaction()) {
            message("Reactions are played when attacked!");
            return false;
        }
        if ((card.getType() == CardType.RAMPAGE || card.getType() == CardType.HEAL) && !hand.hasThreeOfKind()) {
            message("Need a 3-of-a-kind set to unlock Power cards!");
            return false;
        }

        hand.removeAt(handIndex);
        discard.push(card);
        lastPlayed = card.getType();
        listener.onCardPlayed(card.getType());

        switch (card.getType()) {
            case HIT:
                startAttack(player, opponent(player), CardType.HIT, 1);
                break;
            case ASSASSIN:
                startAttack(player, opponent(player), CardType.ASSASSIN, 2);
                break;
            case RAMPAGE:
                startAttack(player, opponent(player), CardType.RAMPAGE, 2);
                break;
            case HEAL:
                player.heal(2);
                message(player.isHuman() ? "You heal +2!" : "Bot heals +2!");
                endTurn();
                break;
            case TRANSFER:
                player.heal(1);
                startAttack(player, opponent(player), CardType.TRANSFER, 1);
                break;
            default:
                endTurn();
                break;
        }
        listener.onStateChanged();
        return true;
    }

    public boolean activateSet(Player player, CardType setType, CardType power) {
        if (phase == Phase.GAME_OVER || awaitingReaction || player != current) return false;
        if (!player.getHand().hasThree(setType)) return false;

        List<Card> taken = player.getHand().takeMatching(setType, 3);
        discard.pushAll(taken);
        lastPlayed = power;
        listener.onCardPlayed(power);

        if (power == CardType.RAMPAGE) {
            message(player.isHuman() ? "RAMPAGE!" : "Bot goes on a RAMPAGE!");
            startAttack(player, opponent(player), CardType.RAMPAGE, 2);
        } else {
            player.heal(2);
            message(player.isHuman() ? "HEAL set! +2 HP" : "Bot HEAL set! +2 HP");
            endTurn();
        }
        listener.onStateChanged();
        return true;
    }

    public boolean tryReact(Player player, int handIndex) {
        if (!awaitingReaction || player != attackTarget) return false;
        Hand hand = player.getHand();
        if (handIndex < 0 || handIndex >= hand.size()) return false;
        Card card = hand.get(handIndex);
        if (!card.getType().isReaction()) return false;

        hand.removeAt(handIndex);
        discard.push(card);
        lastPlayed = card.getType();
        listener.onCardPlayed(card.getType());
        message(card.getType() == CardType.DODGE ? "DODGE!" : "DENIED!");
        resolveAttack(true);
        return true;
    }

    public void endTurn() {
        if (phase == Phase.GAME_OVER) return;
        refill(current);
        current = opponent(current);
        refill(current);
        awaitingReaction = false;
        phase = current.isHuman() ? Phase.HUMAN_TURN : Phase.BOT_TURN;
        if (current.isHuman()) message("Your turn");
        listener.onStateChanged();
    }

    public void update(float delta) {
        if (phase == Phase.GAME_OVER || !awaitingReaction) return;
        reactionTimer -= delta;
        if (reactionTimer <= 0f) resolveAttack(false);
    }

    private void startAttack(Player source, Player target, CardType type, int damage) {
        awaitingReaction = true;
        attackSource = source;
        attackTarget = target;
        attackType = type;
        pendingDamage = damage;
        reactionTimer = REACTION_SECONDS;
        phase = Phase.AWAITING_REACTION;
        if (target.isHuman()) message("React with DODGE or DENIED!");
        listener.onStateChanged();
    }

    public void resolveAttack(boolean blocked) {
        awaitingReaction = false;
        if (!blocked) {
            attackTarget.damage(pendingDamage);
            if (attackTarget.isEliminated()) {
                message("BRUHAHA!");
                phase = Phase.GAME_OVER;
                winner = attackSource;
                listener.onGameOver(winner);
                listener.onStateChanged();
                return;
            }
            message(attackTarget.isHuman()
                ? "You took " + pendingDamage + " damage!"
                : "Bot took " + pendingDamage + " damage!");
        }
        endTurn();
    }

    private Player opponent(Player p) {
        return p == human ? bot : human;
    }

    private void message(String text) {
        listener.onMessage(text);
    }

    public Player getHuman() { return human; }
    public Player getBot() { return bot; }
    public Player getCurrent() { return current; }
    public Player getWinner() { return winner; }
    public Phase getPhase() { return phase; }
    public Deck getDeck() { return deck; }
    public CardPile getDiscard() { return discard; }
    public CardType getLastPlayed() { return lastPlayed; }
    public boolean isAwaitingReaction() { return awaitingReaction; }
    public Player getAttackTarget() { return attackTarget; }
    public Player getAttackSource() { return attackSource; }
    public CardType getAttackType() { return attackType; }
    public int getPendingDamage() { return pendingDamage; }
    public float getReactionTimer() { return reactionTimer; }

    public Array<Integer> reactionIndices(Player player) {
        Array<Integer> indices = new Array<>();
        Hand hand = player.getHand();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getType().isReaction()) indices.add(i);
        }
        return indices;
    }
}
