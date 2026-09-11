package dev.workstho.bruhaha;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kotcrab.vis.ui.VisUI;
import dev.workstho.bruhaha.assets.GameAssets;
import dev.workstho.bruhaha.screens.SplashScreen;

/** Application entry — Rockstar-style splash, then straight into gameplay. */
public class BruhahaGame extends Game {
    private SpriteBatch batch;
    private GameAssets assets;

    @Override
    public void create() {
        VisUI.load(VisUI.SkinScale.X2);
        batch = new SpriteBatch();
        assets = new GameAssets();
        assets.queue();
        setScreen(new SplashScreen(this));
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public GameAssets getAssets() {
        return assets;
    }

    @Override
    public void dispose() {
        if (screen != null) screen.dispose();
        if (assets != null) assets.dispose();
        if (batch != null) batch.dispose();
        if (VisUI.isLoaded()) VisUI.dispose();
    }
}
