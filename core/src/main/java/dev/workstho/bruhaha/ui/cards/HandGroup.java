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
 * Overlapping fan hand layout with animated rearrange.
 * Inspired by community Scene2D hand patterns (negative overlap + rotation fan)
 * and P2Poker {@code CardActor} draw/transform style.
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

    public int getHoverIndex() {
        return hoverIndex;
    }

    public void setHoverIndex(int index) {
        if (hoverIndex == index) return;
        int prev = hoverIndex;
        hoverIndex = index;
        if (prev >= 0 && prev < cards.size) cards.get(prev).setHighlighted(false);
        if (hoverIndex >= 0 && hoverIndex < cards.size) cards.get(hoverIndex).setHighlighted(true);
        animateFan(0.18f, false);
    }

    /** Rebuild actors from model hand; optionally deal-animate from a deck point. */
    public void syncFromHand(Hand hand, float deckX, float deckY, boolean dealAnimate) {
        clearChildren();
        cards.clear();
        hoverIndex = -1;
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            CardActor actor = new CardActor(card, assets.face(card.getType()), assets.back(), faceDown);
            actor.setHandIndex(i);
            actor.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                @Override
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                    if (clickListener != null) clickListener.onCardClicked(actor, actor.getHandIndex());
                    return true;
                }

                @Override
                public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                    if (!faceDown) setHoverIndex(actor.getHandIndex());
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

            FanSlot slot = slotFor(i, hand.size());
            if (dealAnimate) {
                actor.setPosition(deckX, deckY);
                actor.setRotation(MathUtils.random(-12f, 12f));
                actor.setScale(0.55f);
                actor.getColor().a = 0f;
                actor.addAction(Actions.sequence(
                    Actions.delay(i * 0.07f),
                    Actions.parallel(
                        Actions.fadeIn(0.15f),
                        Actions.scaleTo(1f, 1f, 0.32f, Interpolation.swingOut),
                        ArcToAction.arcTo(slot.x, slot.y, 0.42f, Interpolation.swingOut, slot.rotation)
                    )
                ));
            } else {
                actor.setPosition(slot.x, slot.y);
                actor.setRotation(slot.rotation);
            }
        }
    }

    public void animateFan(float duration, boolean staggered) {
        int n = cards.size;
        for (int i = 0; i < n; i++) {
            CardActor actor = cards.get(i);
            FanSlot slot = slotFor(i, n);
            float lift = (!faceDown && i == hoverIndex) ? 34f : 0f;
            actor.clearActions();
            float delay = staggered ? i * 0.03f : 0f;
            actor.addAction(Actions.sequence(
                Actions.delay(delay),
                Actions.parallel(
                    Actions.moveTo(slot.x, slot.y + lift, duration, Interpolation.smooth),
                    Actions.rotateTo(slot.rotation, duration, Interpolation.smooth),
                    Actions.scaleTo(i == hoverIndex ? 1.08f : 1f, i == hoverIndex ? 1.08f : 1f, duration, Interpolation.smooth)
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
        // Overlap spacing shrinks as hand grows (community hand-arrangement pattern).
        float maxSpan = Math.min(980f, getStage() != null ? getStage().getWidth() - 160f : 980f);
        float idealStep = CardActor.BASE_W * 0.62f;
        float step = count <= 1 ? 0f : Math.min(idealStep, maxSpan / (count - 1f));
        float total = step * (count - 1);
        float startX = centerX - total / 2f - CardActor.BASE_W / 2f;

        float mid = (count - 1) * 0.5f;
        float fanAngle = fan ? MathUtils.clamp(count * 2.2f, 0f, 18f) : 0f;
        float t = count == 1 ? 0f : (index - mid) / mid;
        slot.rotation = fan ? -t * fanAngle : 0f;
        slot.x = startX + index * step;
        slot.y = layoutY - (fan ? Math.abs(t) * 10f : 0f);
        return slot;
    }

    private static class FanSlot {
        float x, y, rotation;
    }
}
