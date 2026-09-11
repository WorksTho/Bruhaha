package dev.workstho.bruhaha.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import dev.workstho.bruhaha.assets.GameAssets;
import dev.workstho.bruhaha.gameplay.GameSession;
import dev.workstho.bruhaha.model.CardType;
import dev.workstho.bruhaha.model.Player;

/** HD gameplay HUD — instructions at top (no title), scores on the sides. */
public class GameHud implements Disposable {
    public interface Actions {
        void onEndTurn();
        void onRampageSet();
        void onHealSet();
        void onPlayAgain();
    }

    private final Stage stage;
    private final Actions actions;
    private final Array<Texture> ownedTextures = new Array<>();

    private final Label status;
    private final Label humanHpLabel;
    private final Label botHpLabel;
    private final Label deckLabel;
    private final ProgressBar humanHpBar;
    private final ProgressBar botHpBar;
    private final TextButton endTurn;
    private final TextButton rampageSet;
    private final TextButton healSet;
    private final TextButton playAgain;

    public GameHud(GameAssets assets, Actions actions) {
        this.actions = actions;
        stage = new Stage(new FitViewport(1600, 900));

        Label.LabelStyle statusStyle = style(assets.statusFont(), Color.WHITE);
        Label.LabelStyle scoreStyle = style(assets.scoreFont(), Color.WHITE);
        Label.LabelStyle deckStyle = style(assets.hudFont(), new Color(0.85f, 0.88f, 0.95f, 1f));

        status = new Label("", statusStyle);
        status.setAlignment(Align.center);
        humanHpLabel = new Label("YOU", scoreStyle);
        botHpLabel = new Label("BOT", scoreStyle);
        deckLabel = new Label("DECK 0", deckStyle);

        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = solidDrawable(new Color(0.18f, 0.18f, 0.24f, 0.95f), 8, 22);
        barStyle.knobBefore = solidDrawable(new Color(0.28f, 0.86f, 0.42f, 1f), 8, 22);
        humanHpBar = new ProgressBar(0, GameSession.MAX_HP, 1, false, barStyle);
        botHpBar = new ProgressBar(0, GameSession.MAX_HP, 1, false, cloneBarStyle(barStyle));
        humanHpBar.setValue(GameSession.MAX_HP);
        botHpBar.setValue(GameSession.MAX_HP);
        humanHpBar.setAnimateDuration(0.2f);
        botHpBar.setAnimateDuration(0.2f);

        TextButton.TextButtonStyle dangerBtn = buttonStyle(assets.buttonFont(),
            new Color(0.86f, 0.18f, 0.30f, 1f), new Color(0.98f, 0.32f, 0.42f, 1f));
        TextButton.TextButtonStyle rampageBtn = buttonStyle(assets.buttonFont(),
            new Color(0.55f, 0.08f, 0.18f, 1f), new Color(0.78f, 0.14f, 0.28f, 1f));
        TextButton.TextButtonStyle healBtn = buttonStyle(assets.buttonFont(),
            new Color(0.95f, 0.78f, 0.12f, 1f), new Color(1f, 0.9f, 0.3f, 1f));
        healBtn.fontColor = new Color(0.12f, 0.08f, 0.02f, 1f);
        healBtn.overFontColor = new Color(0.05f, 0.05f, 0.05f, 1f);

        endTurn = new TextButton("END TURN", dangerBtn);
        rampageSet = new TextButton("SET: RAMPAGE", rampageBtn);
        healSet = new TextButton("SET: HEAL", healBtn);
        playAgain = new TextButton("PLAY AGAIN", dangerBtn);

        endTurn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { actions.onEndTurn(); }
        });
        rampageSet.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { actions.onRampageSet(); }
        });
        healSet.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { actions.onHealSet(); }
        });
        playAgain.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) { actions.onPlayAgain(); }
        });

        // Top: BOT | instructions (title space) | DECK — keeps status clear of bot cards
        Table topBar = new Table();
        topBar.setFillParent(true);
        topBar.top().padTop(10f).padLeft(18f).padRight(18f);
        Table topRow = new Table();
        topRow.add(botHpLabel).left().padRight(8f);
        topRow.add(botHpBar).width(200f).height(20f).padRight(16f);
        topRow.add(status).expandX().center().padLeft(8f).padRight(8f);
        topRow.add(deckLabel).right();
        topBar.add(topRow).growX().height(64f);

        Table bottomBar = new Table();
        bottomBar.setFillParent(true);
        bottomBar.bottom().pad(14f);
        Table bottomRow = new Table();
        bottomRow.add(humanHpLabel).padRight(8f);
        bottomRow.add(humanHpBar).width(200f).height(20f).padRight(14f);
        bottomRow.add(rampageSet).height(54f).padRight(8f);
        bottomRow.add(healSet).height(54f).padRight(8f);
        bottomRow.add().expandX();
        bottomRow.add(endTurn).height(54f).padRight(8f);
        bottomRow.add(playAgain).height(54f);
        bottomBar.add(bottomRow).growX();

        stage.addActor(topBar);
        stage.addActor(bottomBar);

        rampageSet.setVisible(false);
        healSet.setVisible(false);
        playAgain.setVisible(false);
    }

    private Label.LabelStyle style(BitmapFont font, Color color) {
        Label.LabelStyle s = new Label.LabelStyle();
        s.font = font;
        s.fontColor = color;
        return s;
    }

    private TextButton.TextButtonStyle buttonStyle(BitmapFont font, Color up, Color over) {
        TextButton.TextButtonStyle s = new TextButton.TextButtonStyle();
        s.font = font;
        s.fontColor = Color.WHITE;
        s.overFontColor = Color.WHITE;
        s.downFontColor = new Color(1f, 1f, 1f, 0.9f);
        s.up = roundDrawable(up);
        s.over = roundDrawable(over);
        s.down = roundDrawable(up.cpy().mul(0.85f));
        return s;
    }

    private NinePatchDrawable roundDrawable(Color color) {
        int w = 48;
        int h = 48;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        pm.setColor(color);
        pm.fillRectangle(8, 0, w - 16, h);
        pm.fillRectangle(0, 8, w, h - 16);
        pm.fillCircle(8, 8, 8);
        pm.fillCircle(w - 9, 8, 8);
        pm.fillCircle(8, h - 9, 8);
        pm.fillCircle(w - 9, h - 9, 8);
        Texture tex = new Texture(pm);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        ownedTextures.add(tex);
        pm.dispose();
        NinePatch patch = new NinePatch(new TextureRegion(tex), 12, 12, 12, 12);
        return new NinePatchDrawable(patch);
    }

    private TextureRegionDrawable solidDrawable(Color color, int w, int h) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture tex = new Texture(pm);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        ownedTextures.add(tex);
        pm.dispose();
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    private ProgressBar.ProgressBarStyle cloneBarStyle(ProgressBar.ProgressBarStyle src) {
        ProgressBar.ProgressBarStyle s = new ProgressBar.ProgressBarStyle();
        s.background = src.background;
        s.knobBefore = src.knobBefore;
        return s;
    }

    public Stage getStage() {
        return stage;
    }

    public void refresh(GameSession session, String message) {
        Player human = session.getHuman();
        Player bot = session.getBot();
        humanHpBar.setValue(human.getHp());
        botHpBar.setValue(bot.getHp());
        humanHpLabel.setText("YOU  " + human.getHp() + "/" + human.getMaxHp());
        botHpLabel.setText("BOT  " + bot.getHp() + "/" + bot.getMaxHp());
        deckLabel.setText("DECK " + (session.getDeck() != null ? session.getDeck().size() : 0));

        boolean humanTurn = session.getPhase() == GameSession.Phase.HUMAN_TURN;
        boolean gameOver = session.getPhase() == GameSession.Phase.GAME_OVER;
        endTurn.setVisible(humanTurn);
        rampageSet.setVisible(humanTurn && human.getHand().hasThree(CardType.HIT));
        healSet.setVisible(humanTurn && human.getHand().hasThree(CardType.DODGE));
        playAgain.setVisible(gameOver);

        if (session.isAwaitingReaction() && session.getAttackTarget().isHuman()) {
            status.setText("Incoming " + session.getAttackType().label + " (" + session.getPendingDamage()
                + ") — click DODGE / DENIED!");
            status.setColor(0.45f, 0.9f, 1f, 1f);
        } else if (session.isAwaitingReaction()) {
            status.setText("BOT IS REACTING...");
            status.setColor(0.85f, 0.85f, 0.9f, 1f);
        } else if (gameOver) {
            status.setText(session.getWinner().isHuman() ? "YOU WIN — BRUHAHA!" : "BOT WINS — BRUHAHA!");
            status.setColor(1f, 0.86f, 0.18f, 1f);
        } else if (message != null && !message.isEmpty()) {
            status.setText(message.toUpperCase());
            status.setColor(1f, 0.92f, 0.45f, 1f);
        } else if (humanTurn) {
            status.setText("YOUR TURN — SELECT A CARD!");
            status.setColor(1f, 1f, 1f, 1f);
        } else {
            status.setText("BOT IS THINKING...");
            status.setColor(0.8f, 0.82f, 0.9f, 1f);
        }
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void act(float delta) {
        stage.act(delta);
    }

    public void draw() {
        stage.getViewport().apply();
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        for (Texture t : ownedTextures) t.dispose();
        ownedTextures.clear();
    }
}
