package dev.workstho.bruhaha.ui.cards;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.math.Interpolation;
import dev.workstho.bruhaha.model.Card;
import dev.workstho.bruhaha.model.CardType;

/**
 * Scene2D card actor (pattern from P2Poker {@code CardActor}).
 * Draws designed HD face/back textures with origin-centered transforms for fan/throw anims.
 */
public class CardActor extends Actor {
    public static final float BASE_W = 168f;
    public static final float BASE_H = 236f;

    private Card card;
    private Texture face;
    private Texture back;
    private boolean faceDown;
    private boolean highlighted;
    private int handIndex = -1;

    public CardActor(Card card, Texture face, Texture back, boolean faceDown) {
        this.card = card;
        this.face = face;
        this.back = back;
        this.faceDown = faceDown;
        setSize(BASE_W, BASE_H);
        setOrigin(BASE_W / 2f, BASE_H / 2f);
        setTouchable(Touchable.enabled);
    }

    public void setTextures(Texture face, Texture back) {
        this.face = face;
        this.back = back;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Card getCard() {
        return card;
    }

    public CardType getCardType() {
        return card != null ? card.getType() : null;
    }

    public void setFaceDown(boolean faceDown) {
        this.faceDown = faceDown;
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public void setHandIndex(int handIndex) {
        this.handIndex = handIndex;
    }

    public int getHandIndex() {
        return handIndex;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        clearActions();
        if (highlighted) {
            addAction(Actions.sequence(
                Actions.scaleTo(1.08f, 1.08f, 0.12f, Interpolation.smooth),
                Actions.moveBy(0f, 28f, 0.12f, Interpolation.smooth)
            ));
            setColor(1f, 1f, 0.85f, 1f);
        } else {
            setColor(Color.WHITE);
            setScale(1f);
        }
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    /** Soft pulse used when a reaction is playable. */
    public void pulsePlayable() {
        addAction(Actions.forever(Actions.sequence(
            Actions.scaleTo(1.04f, 1.04f, 0.35f, Interpolation.sine),
            Actions.scaleTo(1f, 1f, 0.35f, Interpolation.sine)
        )));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
        Texture tex = faceDown ? back : face;
        if (tex != null) {
            batch.draw(tex, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation(),
                0, 0, tex.getWidth(), tex.getHeight(), false, false);
        }
        // Drop shadow edge
        if (highlighted) {
            batch.setColor(1f, 0.85f, 0.2f, 0.35f * parentAlpha);
            batch.draw(tex != null ? tex : back, getX() - 4f, getY() - 4f, getOriginX() + 4f, getOriginY() + 4f,
                getWidth() + 8f, getHeight() + 8f, getScaleX(), getScaleY(), getRotation(),
                0, 0, (tex != null ? tex : back).getWidth(), (tex != null ? tex : back).getHeight(), false, false);
            batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
            if (tex != null) {
                batch.draw(tex, getX(), getY(), getOriginX(), getOriginY(),
                    getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation(),
                    0, 0, tex.getWidth(), tex.getHeight(), false, false);
            }
        }
        batch.setColor(Color.WHITE);
    }
}
