package dev.workstho.bruhaha.ui.cards;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;

/**
 * Curved throw/deal path for Scene2D cards.
 * Adapted from the public ArcToAction pattern:
 * https://stackoverflow.com/questions/49877500 (LibGDX Actions curved moveTo)
 */
public class ArcToAction extends MoveToAction {
    private float angle;
    private final Vector2 start = new Vector2();
    private final Vector2 end = new Vector2();
    private final Vector2 pivot = new Vector2();
    private final Vector2 tmp = new Vector2();
    private float startRotation;
    private float endRotation;
    private boolean rotate = true;

    public void setEndRotation(float degrees) {
        this.endRotation = degrees;
    }

    public void setRotateAlongArc(boolean rotate) {
        this.rotate = rotate;
    }

    @Override
    protected void begin() {
        super.begin();
        start.set(target.getX(getAlignment()), target.getY(getAlignment()));
        end.set(getX(), getY());
        pivot.set(start).add(end).scl(0.5f);
        // Offset pivot perpendicular for arc height
        tmp.set(end).sub(start);
        float len = tmp.len();
        if (len < 1f) {
            angle = 0f;
            return;
        }
        tmp.rotate90(1).nor().scl(Math.min(180f, len * 0.35f));
        pivot.add(tmp);
        startRotation = target.getRotation();
        Vector2 ca = tmp.set(start).sub(pivot);
        Vector2 cb = new Vector2(end).sub(pivot);
        angle = ca.angleDeg(cb);
    }

    @Override
    protected void update(float percent) {
        if (percent >= 1f) {
            target.setPosition(end.x, end.y, getAlignment());
            if (rotate) target.setRotation(endRotation);
            return;
        }
        tmp.set(start).sub(pivot).rotateDeg(angle * percent).add(pivot);
        target.setPosition(tmp.x, tmp.y, getAlignment());
        if (rotate) {
            target.setRotation(MathUtils.lerp(startRotation, endRotation, percent));
        }
    }

    public static ArcToAction arcTo(float x, float y, float duration, Interpolation interpolation, float endRotation) {
        ArcToAction action = new ArcToAction();
        action.setPosition(x, y);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        action.setEndRotation(endRotation);
        return action;
    }
}
