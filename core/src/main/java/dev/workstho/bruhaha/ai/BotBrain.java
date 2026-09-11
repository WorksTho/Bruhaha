package dev.workstho.bruhaha.ai;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import dev.workstho.bruhaha.gameplay.GameSession;
import dev.workstho.bruhaha.model.CardType;
import dev.workstho.bruhaha.model.Hand;
import dev.workstho.bruhaha.model.Player;

/**
 * Bot opponent driven by gdx-ai {@link StateMachine}.
 */
public class BotBrain {
    private final GameSession session;
    private final StateMachine<BotBrain, BotState> fsm;
    private float thinkTimer;

    public BotBrain(GameSession session) {
        this.session = session;
        this.fsm = new DefaultStateMachine<>(this, BotState.IDLE);
    }

    public void update(float delta) {
        fsm.update();
        BotState state = fsm.getCurrentState();
        if (state == BotState.IDLE) {
            if (session.getPhase() == GameSession.Phase.BOT_TURN) {
                thinkTimer = 0.75f + MathUtils.random(0.35f);
                fsm.changeState(BotState.THINKING);
            } else if (session.getPhase() == GameSession.Phase.AWAITING_REACTION
                && session.getAttackTarget() == session.getBot()) {
                thinkTimer = 0.45f;
                fsm.changeState(BotState.REACTING);
            }
        } else if (state == BotState.THINKING || state == BotState.REACTING) {
            thinkTimer -= delta;
            if (thinkTimer <= 0f) {
                if (state == BotState.THINKING) chooseAction();
                else chooseReaction();
                fsm.changeState(BotState.IDLE);
            }
        }
    }

    public void reset() {
        fsm.changeState(BotState.IDLE);
        thinkTimer = 0f;
    }

    private void chooseAction() {
        Player bot = session.getBot();
        Player human = session.getHuman();
        Hand hand = bot.getHand();

        if (bot.getHp() <= 2 && hand.hasThree(CardType.DODGE)) {
            session.activateSet(bot, CardType.DODGE, CardType.HEAL);
            return;
        }
        if (bot.getHp() <= 2 && hand.hasThreeOfKind()) {
            int heal = hand.indexOf(CardType.HEAL);
            if (heal >= 0) { session.tryPlay(bot, heal); return; }
        }
        if (human.getHp() <= 2 && hand.hasThree(CardType.HIT)) {
            session.activateSet(bot, CardType.HIT, CardType.RAMPAGE);
            return;
        }

        boolean aggressive = human.getHp() <= 3 || MathUtils.randomBoolean(0.4f);
        int assassin = hand.indexOf(CardType.ASSASSIN);
        int rampage = hand.hasThreeOfKind() ? hand.indexOf(CardType.RAMPAGE) : -1;
        int hit = hand.indexOf(CardType.HIT);
        int transfer = hand.indexOf(CardType.TRANSFER);

        if (assassin >= 0 && (human.getHp() <= 2 || aggressive)) { session.tryPlay(bot, assassin); return; }
        if (rampage >= 0 && aggressive) { session.tryPlay(bot, rampage); return; }
        if (hand.hasThree(CardType.HIT) && (human.getHp() <= 3 || MathUtils.randomBoolean(0.25f))) {
            session.activateSet(bot, CardType.HIT, CardType.RAMPAGE);
            return;
        }
        if (transfer >= 0 && bot.getHp() < bot.getMaxHp() && MathUtils.randomBoolean(0.35f)) {
            session.tryPlay(bot, transfer);
            return;
        }
        if (hit >= 0) { session.tryPlay(bot, hit); return; }
        if (assassin >= 0) { session.tryPlay(bot, assassin); return; }
        if (rampage >= 0) { session.tryPlay(bot, rampage); return; }
        if (transfer >= 0) { session.tryPlay(bot, transfer); return; }
        session.endTurn();
    }

    private void chooseReaction() {
        Player bot = session.getBot();
        Hand hand = bot.getHand();
        int dodge = hand.indexOf(CardType.DODGE);
        int denied = hand.indexOf(CardType.DENIED);
        int pending = session.getPendingDamage();

        boolean should = pending >= bot.getHp() || pending >= 2 || bot.getHp() <= 2 || MathUtils.randomBoolean(0.55f);
        if (!should || (dodge < 0 && denied < 0)) {
            session.resolveAttack(false);
            return;
        }
        int pick = (session.getAttackType() == CardType.ASSASSIN && denied >= 0)
            ? denied : (dodge >= 0 ? dodge : denied);
        session.tryReact(bot, pick);
    }

    public enum BotState implements State<BotBrain> {
        IDLE, THINKING, REACTING;

        @Override public void enter(BotBrain entity) {}
        @Override public void update(BotBrain entity) {}
        @Override public void exit(BotBrain entity) {}
        @Override public boolean onMessage(BotBrain entity, Telegram telegram) { return false; }
    }
}
