package dev.workstho.bruhaha.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import dev.workstho.bruhaha.BruhahaGame;

/** Modern VisUI main menu. */
public class MenuScreen extends ScreenAdapter {
    private final BruhahaGame game;
    private Stage stage;

    public MenuScreen(BruhahaGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(1600, 900), game.getBatch());
        Gdx.input.setInputProcessor(stage);

        VisLabel title = new VisLabel("BRUHAHA");
        title.setColor(1f, 0.85f, 0.2f, 1f);
        VisLabel subtitle = new VisLabel("Fast elimination card combat — You vs Bot");
        VisTextButton play = new VisTextButton("PLAY");
        VisTextButton quit = new VisTextButton("QUIT");

        play.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new GameScreen(game));
            }
        });
        quit.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        VisTable root = new VisTable();
        root.setFillParent(true);
        root.center();
        root.add(title).padBottom(12).row();
        root.add(subtitle).padBottom(36).row();
        root.add(play).width(220).height(48).padBottom(12).row();
        root.add(quit).width(220).height(48);

        stage.addActor(root);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.07f, 0.08f, 0.11f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        stage.dispose();
    }
}
