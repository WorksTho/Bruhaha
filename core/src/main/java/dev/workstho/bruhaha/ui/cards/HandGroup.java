package dev.workstho.bruhaha.ui.cards;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import dev.workstho.bruhaha.assets.GameAssets;
import dev.workstho.bruhaha.model.Card;
import dev.workstho.bruhaha.model.Hand;

/**
 * Overlapping fan hand with deal-from-deck and rearrange animations.
 */
public class HandGroup extends Group {
    public interface CardClickListener {
        void onCardClicked(CardActor actor, int handIndex);
    }

    private final GameAssets assets;
    private final boolean faceDown;
    private final boolean fan;
    private final Array<CardActor> cards = new Array<>();
    private CardClickListener clickListener;
    private float layoutY;
    private float centerX;
    private int hoverIndex = -1;
    private boolean dealing;

    public HandGroup(GameAssets assets, boolean faceDown, boolean fan) {
        this.assets = assets;
        this.faceDown = faceDown;
        this.fan = fan;
        setTransform(false);
    }

    public void setClickListener(CardClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setAnchor(float centerX, float layoutY) {
        this.centerX = centerX;
        this.layoutY = layoutY;
    }

    public Array<CardActor> getCardActors() {
        return cards;
    }

    public boolean isDealing() {
        return dealing;
    }

    public int getHoverIndex() {
        return hoverIndex;
    }

    public void setHoverIndex(int index) {
        if (dealing) return;
        if (hoverIndex == index) return;
        int prev = hoverIndex;
        hoverIndex = index;
        if (prev >= 0 && prev < cards.size) cards.get(prev).setHighlighted(false);
        if (hoverIndex >= 0 && hoverIndex < cards.size) cards.get(hoverIndex).setHighlighted(true);
        animateFan(0.18f, false);
    }

    /**
     * Rebuild from model hand. When {@code dealAnimate}, each card flies from the deck
     * into the rack (face-down for bot; flip to face for player).
     */
    public void syncFromHand(Hand hand, float deckX, float deckY, float deckW, float deckH, boolean dealAnimate) {
        clearChildren();
        cards.clear();
        hoverIndex = -1;
        dealing = dealAnimate;

        int n = hand.size();
        for (int i = 0; i < n; i++) {
            Card card = hand.get(i);
            // Deal starts face-down from the deck, then player cards flip
            CardActor actor = new CardActor(card, assets.face(card.getType()), assets.back(), true);
            actor.setHandIndex(i);
            actor.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                @Override
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                    if (dealing) return false;
                    if (clickListener != null) clickListener.onCardClicked(actor, actor.getHandIndex());
                    return true;
                }

                @Override
                public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                    if (!faceDown && !dealing) setHoverIndex(actor.getHandIndex());
                }

                @Override
                public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                    if (toActor == null || toActor.getParent() != HandGroup.this) {
                        if (hoverIndex == actor.getHandIndex()) setHoverIndex(-1);
                    }
                }
            });
            addActor(actor);
            cards.add(actor);

            FanSlot slot = slotFor(i, n);
            if (dealAnimate) {
                actor.setFaceDown(true);
                actor.setSize(CardActor.BASE_W, CardActor.BASE_H);
                actor.setOrigin(CardActor.BASE_W / 2f, CardActor.BASE_H / 2f);
                actor.setPosition(deckX, deckY);
                actor.setScale(deckW / CardActor.BASE_W);
                actor.setRotation(-4f);
                actor.setTouchable(Touchable.disabled);

                float delay = i * 0.12f;
                final int index = i;
                final boolean flipToFace = !faceDown;
                actor.addAction(Actions.sequence(
                    Actions.delay(delay),
                    Actions.parallel(
                        ArcToAction.arcTo(slot.x, slot.y, 0.45f, Interpolation.sineOut, slot.rotation),
                        Actions.scaleTo(1f, 1f, 0.45f, Interpolation.smooth)
                    ),
                    Actions.run(() -> {
                        actor.setPosition(slot.x, slot.y);
                        actor.setRotation(slot.rotation);
                        actor.setScale(1f);
                    }),
                    flipToFace
                        ? Actions.sequence(
                            Actions.scaleTo(0.02f, 1f, 0.09f, Interpolation.smooth),
                            Actions.run(() -> actor.setFaceDown(false)),
                            Actions.scaleTo(1f, 1f, 0.1f, Interpolation.smooth)
                        )
                        : Actions.delay(0.01f),
                    Actions.run(() -> {
                        actor.setTouchable(Touchable.enabled);
                        if (index == n - 1) dealing = false;
                    })
                ));
            } else {
                actor.setFaceDown(faceDown);
                actor.setPosition(slot.x, slot.y);
                actor.setRotation(slot.rotation);
            }
        }
        if (!dealAnimate || n == 0) dealing = false;
    }

    public void animateFan(float duration, boolean staggered) {
        if (dealing) return;
        int n = cards.size;
        for (int i = 0; i < n; i++) {
            CardActor actor = cards.get(i);
            FanSlot slot = slotFor(i, n);
            float lift = (!faceDown && i == hoverIndex) ? 28f : 0f;
            actor.clearActions();
            float delay = staggered ? i * 0.03f : 0f;
            actor.addAction(Actions.sequence(
                Actions.delay(delay),
                Actions.parallel(
                    Actions.moveTo(slot.x, slot.y + lift, duration, Interpolation.smooth),
                    Actions.rotateTo(slot.rotation, duration, Interpolation.smooth),
                    Actions.scaleTo(i == hoverIndex ? 1.06f : 1f, i == hoverIndex ? 1.06f : 1f, duration, Interpolation.smooth)
                )
            ));
            actor.setZIndex(i == hoverIndex ? n + 5 : i);
        }
    }

    public CardActor takeActor(int handIndex) {
        if (handIndex < 0 || handIndex >= cards.size) return null;
        CardActor actor = cards.removeIndex(handIndex);
        actor.remove();
        for (int i = 0; i < cards.size; i++) cards.get(i).setHandIndex(i);
        return actor;
    }

    private FanSlot slotFor(int index, int count) {
        FanSlot slot = new FanSlot();
        if (count <= 0) {
            slot.x = centerX - CardActor.BASE_W / 2f;
            slot.y = layoutY;
            return slot;
        }
        float maxSpan = Math.min(980f, getStage() != null ? getStage().getWidth() - 160f : 980f);
        float idealStep = CardActor.BASE_W * 0.62f;
        float step = count <= 1 ? 0f : Math.min(idealStep, maxSpan / (count - 1f));
        float total = step * (count - 1);
        float startX = centerX - total / 2f - CardActor.BASE_W / 2f;

        float mid = (count - 1) * 0.5f;
        float fanAngle = fan ? MathUtils.clamp(count * 2.2f, 0f, 16f) : 0f;
        float t = count == 1 ? 0f : (index - mid) / mid;
        slot.rotation = fan ? -t * fanAngle : 0f;
        slot.x = startX + index * step;
        slot.y = layoutY - (fan ? Math.abs(t) * 8f : 0f);
        return slot;
    }

    private static class FanSlot {
        float x, y, rotation;
    }
}
