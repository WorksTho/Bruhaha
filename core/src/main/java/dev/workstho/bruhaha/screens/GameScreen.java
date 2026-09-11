package dev.workstho.bruhaha.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import dev.workstho.bruhaha.BruhahaGame;
import dev.workstho.bruhaha.ai.BotBrain;
import dev.workstho.bruhaha.gameplay.GameSession;
import dev.workstho.bruhaha.model.Card;
import dev.workstho.bruhaha.model.CardType;
import dev.workstho.bruhaha.model.Player;
import dev.workstho.bruhaha.ui.GameHud;
import dev.workstho.bruhaha.ui.cards.CardPlayLayer;
import dev.workstho.bruhaha.view3d.TableWorld;

/**
 * 3D table backdrop + HD Scene2D card layer with deal/fan/throw animations.
 */
public class GameScreen extends ScreenAdapter implements GameSession.Listener, GameHud.Actions, CardPlayLayer.Listener {
    private final BruhahaGame game;
    private GameSession session;
    private BotBrain bot;
    private TableWorld world;
    private CardPlayLayer cards;
    private GameHud hud;
    private String flash = "";
    private float flashTimer;
    private boolean dirty = true;
    private boolean suppressSync;
    private CardType pendingBotThrow;

    public GameScreen(BruhahaGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        session = new GameSession();
        session.setListener(this);
        bot = new BotBrain(session);
        world = new TableWorld(game.getAssets());
        cards = new CardPlayLayer(game.getAssets(), this);
        hud = new GameHud(game.getAssets(), this);

        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(hud.getStage());
        mux.addProcessor(cards.getStage());
        Gdx.input.setInputProcessor(mux);

        cards.requestDealAnimation();
        session.startNewRound();
        bot.reset();
        dirty = true;
    }

    @Override
    public void onPlayerCardChosen(int handIndex) {
        if (cards.isAnimatingThrow() || suppressSync) return;
        Player human = session.getHuman();
        if (handIndex < 0 || handIndex >= human.getHand().size()) return;

        Card card = human.getHand().get(handIndex);
        boolean reacting = session.isAwaitingReaction() && session.getAttackTarget().isHuman();
        if (reacting) {
            if (!card.getType().isReaction()) return;
            suppressSync = true;
            cards.throwFromPlayer(handIndex, card.getType(), () -> {
                session.tryReact(human, handIndex);
                suppressSync = false;
                dirty = true;
            });
            return;
        }

        if (session.getPhase() != GameSession.Phase.HUMAN_TURN) return;
        if (card.getType().isReaction()) {
            onMessage("Reactions are played when attacked!");
            return;
        }
        if ((card.getType() == CardType.RAMPAGE || card.getType() == CardType.HEAL) && !human.getHand().hasThreeOfKind()) {
            onMessage("Need a 3-of-a-kind set to unlock Power cards!");
            return;
        }

        suppressSync = true;
        CardType type = card.getType();
        cards.throwFromPlayer(handIndex, type, () -> {
            session.tryPlay(human, handIndex);
            suppressSync = false;
            dirty = true;
        });
    }

    @Override
    public void render(float delta) {
        if (flashTimer > 0f) flashTimer -= delta;
        if (!cards.isAnimatingThrow()) {
            session.update(delta);
            bot.update(delta);
        }

        if (pendingBotThrow != null && !cards.isAnimatingThrow()) {
            CardType type = pendingBotThrow;
            pendingBotThrow = null;
            suppressSync = true;
            cards.throwFromBot(type, () -> {
                suppressSync = false;
                dirty = true;
            });
        }

        if (dirty && !suppressSync && !cards.isAnimatingThrow() && pendingBotThrow == null) {
            world.sync(session);
            cards.sync(session);
            dirty = false;
        }

        Gdx.gl.glClearColor(0.07f, 0.08f, 0.11f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        world.render();
        cards.act(delta);
        cards.draw();

        String msg = flashTimer > 0f ? flash : "";
        hud.refresh(session, msg);
        hud.act(delta);
        hud.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        world.resize(width, height);
        cards.resize(width, height);
        hud.resize(width, height);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (cards != null) { cards.dispose(); cards = null; }
        if (hud != null) { hud.dispose(); hud = null; }
        if (world != null) { world.dispose(); world = null; }
    }

    @Override
    public void onMessage(String text) {
        flash = text;
        flashTimer = 1.7f;
    }

    @Override
    public void onCardPlayed(CardType type) {
        if (!suppressSync) {
            // Bot / non-pre-animated plays get a throw into center
            pendingBotThrow = type;
        }
        dirty = true;
    }

    @Override
    public void onStateChanged() {
        dirty = true;
    }

    @Override
    public void onGameOver(Player winner) {
        dirty = true;
    }

    @Override
    public void onEndTurn() {
        if (session.getPhase() == GameSession.Phase.HUMAN_TURN && !cards.isAnimatingThrow()) {
            session.endTurn();
            dirty = true;
        }
    }

    @Override
    public void onRampageSet() {
        if (cards.isAnimatingThrow()) return;
        suppressSync = true;
        cards.throwFromPlayer(-1, CardType.RAMPAGE, () -> {
            session.activateSet(session.getHuman(), CardType.HIT, CardType.RAMPAGE);
            suppressSync = false;
            dirty = true;
        });
    }

    @Override
    public void onHealSet() {
        if (cards.isAnimatingThrow()) return;
        // Heal stays in hand area conceptually — still show center flourish
        suppressSync = true;
        cards.throwFromPlayer(-1, CardType.HEAL, () -> {
            session.activateSet(session.getHuman(), CardType.DODGE, CardType.HEAL);
            suppressSync = false;
            dirty = true;
        });
    }

    @Override
    public void onPlayAgain() {
        cards.clearPlayed();
        cards.requestDealAnimation();
        session.startNewRound();
        bot.reset();
        dirty = true;
    }
}
