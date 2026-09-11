package dev.workstho.bruhaha.ui.cards;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.FitViewport;
import dev.workstho.bruhaha.assets.GameAssets;
import dev.workstho.bruhaha.gameplay.GameSession;
import dev.workstho.bruhaha.model.Card;
import dev.workstho.bruhaha.model.CardType;

/**
 * Full-screen HD card layer: fan hands, deck, throw-to-center animations.
 * Uses Scene2D Actions + ArcToAction (P2Poker CardActor + public arc-move pattern).
 */
public class CardPlayLayer {
    public interface Listener {
        void onPlayerCardChosen(int handIndex);
    }

    public static final float WIDTH = 1600f;
    public static final float HEIGHT = 900f;

    private final Stage stage;
    private final GameAssets assets;
    private final Listener listener;
    private final HandGroup playerHand;
    private final HandGroup botHand;
    private final Group fxLayer;
    private final Image deckImage;
    private CardActor playedCard;
    private boolean dealOnNextSync = true;
    private boolean animatingThrow;

    public CardPlayLayer(GameAssets assets, Listener listener) {
        this.assets = assets;
        this.listener = listener;
        stage = new Stage(new FitViewport(WIDTH, HEIGHT));
        fxLayer = new Group();

        deckImage = new Image(assets.back());
        deckImage.setSize(CardActor.BASE_W * 0.9f, CardActor.BASE_H * 0.9f);
        deckImage.setPosition(64f, HEIGHT / 2f - deckImage.getHeight() / 2f);
        deckImage.setOrigin(deckImage.getWidth() / 2f, deckImage.getHeight() / 2f);

        playerHand = new HandGroup(assets, false, true);
        botHand = new HandGroup(assets, true, true);
        playerHand.setAnchor(WIDTH / 2f, 42f);
        botHand.setAnchor(WIDTH / 2f, HEIGHT - CardActor.BASE_H - 64f);
        playerHand.setClickListener((actor, handIndex) -> {
            if (!animatingThrow) listener.onPlayerCardChosen(handIndex);
        });

        stage.addActor(deckImage);
        stage.addActor(botHand);
        stage.addActor(playerHand);
        stage.addActor(fxLayer);
    }

    public Stage getStage() {
        return stage;
    }

    public void requestDealAnimation() {
        dealOnNextSync = true;
    }

    public void sync(GameSession session) {
        if (animatingThrow) return;
        float deckX = deckImage.getX();
        float deckY = deckImage.getY();
        boolean deal = dealOnNextSync;
        dealOnNextSync = false;

        playerHand.setAnchor(WIDTH / 2f, 42f);
        botHand.setAnchor(WIDTH / 2f, HEIGHT - CardActor.BASE_H - 64f);
        playerHand.syncFromHand(session.getHuman().getHand(), deckX, deckY, deal);
        botHand.syncFromHand(session.getBot().getHand(), deckX, deckY, deal);

        if (!deal) {
            playerHand.animateFan(0.28f, true);
            botHand.animateFan(0.28f, true);
        }

        if (session.isAwaitingReaction() && session.getAttackTarget().isHuman()) {
            for (CardActor a : playerHand.getCardActors()) {
                if (a.getCardType() != null && a.getCardType().isReaction()) a.pulsePlayable();
            }
        }
    }

    public void clearPlayed() {
        if (playedCard != null) {
            playedCard.remove();
            playedCard = null;
        }
    }

    public void throwFromPlayer(int handIndex, CardType type, Runnable after) {
        CardActor flying = playerHand.takeActor(handIndex);
        if (flying == null) {
            flying = new CardActor(new Card(type, -1), assets.face(type), assets.back(), false);
            flying.setPosition(deckImage.getX(), deckImage.getY());
            flying.setOrigin(CardActor.BASE_W / 2f, CardActor.BASE_H / 2f);
        } else {
            flying.setFaceDown(false);
            flying.setTextures(assets.face(type), assets.back());
        }
        animatingThrow = true;
        fxLayer.addActor(flying);
        flying.toFront();
        flying.clearActions();
        float targetX = WIDTH / 2f - CardActor.BASE_W / 2f;
        float targetY = HEIGHT / 2f - CardActor.BASE_H / 2f + 24f;
        final CardActor fly = flying;
        flying.addAction(Actions.sequence(
            Actions.parallel(
                ArcToAction.arcTo(targetX, targetY, 0.48f, Interpolation.exp5Out, MathUtils.random(-10f, 10f)),
                Actions.scaleTo(1.22f, 1.22f, 0.48f, Interpolation.smooth)
            ),
            Actions.run(() -> {
                if (playedCard != null && playedCard != fly) playedCard.remove();
                playedCard = fly;
                animatingThrow = false;
                playerHand.animateFan(0.26f, true);
                if (after != null) after.run();
            })
        ));
    }

    public void throwFromBot(CardType type, Runnable after) {
        animatingThrow = true;
        CardActor flying = new CardActor(new Card(type, -1), assets.face(type), assets.back(), false);
        flying.setPosition(WIDTH / 2f - CardActor.BASE_W / 2f, HEIGHT - CardActor.BASE_H - 36f);
        flying.setOrigin(CardActor.BASE_W / 2f, CardActor.BASE_H / 2f);
        flying.setScale(0.92f);
        fxLayer.addActor(flying);
        float targetX = WIDTH / 2f - CardActor.BASE_W / 2f;
        float targetY = HEIGHT / 2f - CardActor.BASE_H / 2f + 24f;
        final CardActor fly = flying;
        flying.addAction(Actions.sequence(
            Actions.parallel(
                ArcToAction.arcTo(targetX, targetY, 0.42f, Interpolation.exp5Out, 0f),
                Actions.scaleTo(1.2f, 1.2f, 0.42f, Interpolation.smooth)
            ),
            Actions.run(() -> {
                if (playedCard != null && playedCard != fly) playedCard.remove();
                playedCard = fly;
                animatingThrow = false;
                botHand.animateFan(0.22f, true);
                if (after != null) after.run();
            })
        ));
    }

    public boolean isAnimatingThrow() {
        return animatingThrow;
    }

    public void act(float delta) {
        stage.act(delta);
    }

    public void draw() {
        stage.getViewport().apply();
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }
}
