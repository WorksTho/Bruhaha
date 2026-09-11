package dev.workstho.bruhaha.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import com.kotcrab.vis.ui.widget.VisTable;
import dev.workstho.bruhaha.BruhahaGame;
import dev.workstho.bruhaha.assets.GameAssets;

/** AssetManager loading splash with VisUI progress. */
public class LoadingScreen extends ScreenAdapter {
    private final BruhahaGame game;
    private Stage stage;
    private VisProgressBar bar;
    private VisLabel label;
    private boolean finished;

    public LoadingScreen(BruhahaGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(1600, 900), game.getBatch());
        bar = new VisProgressBar(0f, 1f, 0.01f, false);
        label = new VisLabel("Loading Bruhaha...");
        VisTable root = new VisTable();
        root.setFillParent(true);
        root.center();
        root.add(label).padBottom(16).row();
        root.add(bar).width(420).height(22);
        stage.addActor(root);
    }

    @Override
    public void render(float delta) {
        GameAssets assets = game.getAssets();
        boolean done = assets.update();
        float p = assets.getProgress();
        bar.setValue(p);
        label.setText("Loading Bruhaha... " + MathUtils.round(p * 100) + "%");

        Gdx.gl.glClearColor(0.06f, 0.07f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();

        if (done && !finished) {
            finished = true;
            assets.finishLoading();
            game.setScreen(new MenuScreen(game));
        }
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
