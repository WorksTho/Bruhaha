package dev.workstho.bruhaha.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import dev.workstho.bruhaha.gameplay.GameSession;
import dev.workstho.bruhaha.model.CardType;
import dev.workstho.bruhaha.model.Player;

/** Modern VisUI HUD over the 3D table (Makao-style UI layer). */
public class GameHud {
    public interface Actions {
        void onEndTurn();
        void onRampageSet();
        void onHealSet();
        void onPlayAgain();
    }

    private final Stage stage;
    private final Actions actions;
    private final VisLabel title;
    private final VisLabel status;
    private final VisLabel humanHpLabel;
    private final VisLabel botHpLabel;
    private final VisLabel deckLabel;
    private final VisProgressBar humanHpBar;
    private final VisProgressBar botHpBar;
    private final VisTextButton endTurn;
    private final VisTextButton rampageSet;
    private final VisTextButton healSet;
    private final VisTextButton playAgain;
    private final VisTable bottomBar;
    private final VisTable topBar;

    public GameHud(Actions actions) {
        this.actions = actions;
        stage = new Stage(new FitViewport(1600, 900));

        title = new VisLabel("BRUHAHA");
        title.setColor(1f, 0.85f, 0.2f, 1f);
        status = new VisLabel("");
        humanHpLabel = new VisLabel("YOU");
        botHpLabel = new VisLabel("BOT");
        deckLabel = new VisLabel("DECK 0");

        humanHpBar = new VisProgressBar(0, GameSession.MAX_HP, 1, false);
        botHpBar = new VisProgressBar(0, GameSession.MAX_HP, 1, false);
        humanHpBar.setValue(GameSession.MAX_HP);
        botHpBar.setValue(GameSession.MAX_HP);

        endTurn = new VisTextButton("END TURN");
        rampageSet = new VisTextButton("SET: RAMPAGE");
        healSet = new VisTextButton("SET: HEAL");
        playAgain = new VisTextButton("PLAY AGAIN");

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

        topBar = new VisTable();
        topBar.setFillParent(true);
        topBar.top().pad(16);
        VisTable topRow = new VisTable();
        topRow.add(botHpLabel).left().padRight(8);
        topRow.add(botHpBar).width(180).height(18).padRight(12);
        topRow.add(title).expandX().center();
        topRow.add(deckLabel).padRight(16);
        topBar.add(topRow).growX();
        topBar.row();
        topBar.add(status).padTop(8);

        bottomBar = new VisTable();
        bottomBar.setFillParent(true);
        bottomBar.bottom().pad(16);
        VisTable bottomRow = new VisTable();
        bottomRow.add(humanHpLabel).padRight(8);
        bottomRow.add(humanHpBar).width(180).height(18).padRight(16);
        bottomRow.add(rampageSet).padRight(8);
        bottomRow.add(healSet).padRight(8);
        bottomRow.add().expandX();
        bottomRow.add(endTurn).padRight(8);
        bottomRow.add(playAgain);
        bottomBar.add(bottomRow).growX();

        stage.addActor(topBar);
        stage.addActor(bottomBar);

        rampageSet.setVisible(false);
        healSet.setVisible(false);
        playAgain.setVisible(false);
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
                + ") — click DODGE / DENIED");
        } else if (session.isAwaitingReaction()) {
            status.setText("Bot is reacting...");
        } else if (gameOver) {
            status.setText(session.getWinner().isHuman() ? "YOU WIN — BRUHAHA!" : "BOT WINS — BRUHAHA!");
        } else if (message != null && !message.isEmpty()) {
            status.setText(message);
        } else if (humanTurn) {
            status.setText("YOUR TURN — select a card");
        } else {
            status.setText("BOT IS THINKING...");
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

    public void dispose() {
        stage.dispose();
    }
}
