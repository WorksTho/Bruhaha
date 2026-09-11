package dev.workstho.bruhaha.ui.cards;

import com.badlogic.gdx.math.Interpolation;
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
 * HD card layer: deal-from-deck into racks, neat play-to-desk placement.
 */
public class CardPlayLayer {
    public interface Listener {
        void onPlayerCardChosen(int handIndex);
    }

    public static final float WIDTH = 1600f;
    public static final float HEIGHT = 900f;

    // Leave clear band under top instructions (~80px) and above bottom buttons
    private static final float PLAYER_HAND_Y = 78f;
    private static final float BOT_HAND_Y = HEIGHT - CardActor.BASE_H - 130f;
    private static final float DESK_X = WIDTH / 2f - CardActor.BASE_W / 2f;
    private static final float DESK_Y = HEIGHT / 2f - CardActor.BASE_H / 2f - 10f;

    private final Stage stage;
    private final GameAssets assets;
    private final Listener listener;
    private final HandGroup playerHand;
    private final HandGroup botHand;
    private final Group fxLayer;
    private final Image deckImage;
    private CardActor playedCard;
    private boolean dealOnNextSync = true;
    private boolean animatingPlay;

    public CardPlayLayer(GameAssets assets, Listener listener) {
        this.assets = assets;
        this.listener = listener;
        stage = new Stage(new FitViewport(WIDTH, HEIGHT));
        fxLayer = new Group();

        deckImage = new Image(assets.back());
        deckImage.setSize(CardActor.BASE_W * 0.88f, CardActor.BASE_H * 0.88f);
        deckImage.setPosition(56f, HEIGHT / 2f - deckImage.getHeight() / 2f - 20f);
        deckImage.setOrigin(deckImage.getWidth() / 2f, deckImage.getHeight() / 2f);

        playerHand = new HandGroup(assets, false, true);
        botHand = new HandGroup(assets, true, true);
        playerHand.setAnchor(WIDTH / 2f, PLAYER_HAND_Y);
        botHand.setAnchor(WIDTH / 2f, BOT_HAND_Y);
        playerHand.setClickListener((actor, handIndex) -> {
            if (!isBusy()) listener.onPlayerCardChosen(handIndex);
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
        if (animatingPlay) return;
        float deckX = deckImage.getX();
        float deckY = deckImage.getY();
        float deckW = deckImage.getWidth();
        float deckH = deckImage.getHeight();
        boolean deal = dealOnNextSync;
        dealOnNextSync = false;

        playerHand.setAnchor(WIDTH / 2f, PLAYER_HAND_Y);
        botHand.setAnchor(WIDTH / 2f, BOT_HAND_Y);
        // Stagger bot deal slightly after player for a clear deal rhythm
        playerHand.syncFromHand(session.getHuman().getHand(), deckX, deckY, deckW, deckH, deal);
        botHand.syncFromHand(session.getBot().getHand(), deckX, deckY, deckW, deckH, deal);

        if (!deal) {
            playerHand.animateFan(0.24f, true);
            botHand.animateFan(0.24f, true);
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

    /**
     * Neat play: lift from hand → slide onto desk → soft settle (no wild spin).
     */
    public void throwFromPlayer(int handIndex, CardType type, Runnable after) {
        CardActor flying = playerHand.takeActor(handIndex);
        if (flying == null) {
            flying = new CardActor(new Card(type, -1), assets.face(type), assets.back(), false);
            flying.setPosition(DESK_X, PLAYER_HAND_Y);
            flying.setOrigin(CardActor.BASE_W / 2f, CardActor.BASE_H / 2f);
        } else {
            flying.setFaceDown(false);
            flying.setTextures(assets.face(type), assets.back());
        }
        placeOnDesk(flying, true, after);
    }

    public void throwFromBot(CardType type, Runnable after) {
        CardActor flying = new CardActor(new Card(type, -1), assets.face(type), assets.back(), false);
        flying.setPosition(WIDTH / 2f - CardActor.BASE_W / 2f, BOT_HAND_Y);
        flying.setOrigin(CardActor.BASE_W / 2f, CardActor.BASE_H / 2f);
        flying.setScale(0.95f);
        placeOnDesk(flying, false, after);
    }

    private void placeOnDesk(CardActor flying, boolean fromPlayer, Runnable after) {
        animatingPlay = true;
        fxLayer.addActor(flying);
        flying.toFront();
        flying.clearActions();
        flying.setHighlighted(false);
        final CardActor fly = flying;

        // 1) lift  2) glide to desk  3) settle
        fly.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveBy(0f, fromPlayer ? 36f : -28f, 0.12f, Interpolation.smooth),
                Actions.scaleTo(1.05f, 1.05f, 0.12f, Interpolation.smooth),
                Actions.rotateTo(0f, 0.12f, Interpolation.smooth)
            ),
            Actions.parallel(
                Actions.moveTo(DESK_X, DESK_Y, 0.34f, Interpolation.sineOut),
                Actions.scaleTo(1.12f, 1.12f, 0.34f, Interpolation.smooth)
            ),
            Actions.scaleTo(1.08f, 1.08f, 0.1f, Interpolation.smooth),
            Actions.run(() -> {
                if (playedCard != null && playedCard != fly) {
                    playedCard.addAction(Actions.sequence(
                        Actions.fadeOut(0.15f),
                        Actions.removeActor()
                    ));
                }
                playedCard = fly;
                fly.getColor().a = 1f;
                animatingPlay = false;
                if (fromPlayer) playerHand.animateFan(0.22f, true);
                else botHand.animateFan(0.2f, true);
                if (after != null) after.run();
            })
        ));
    }

    public boolean isAnimatingThrow() {
        return animatingPlay || playerHand.isDealing() || botHand.isDealing();
    }

    public boolean isBusy() {
        return isAnimatingThrow();
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
