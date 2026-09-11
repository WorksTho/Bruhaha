package dev.workstho.bruhaha.view3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.utils.Disposable;
import dev.workstho.bruhaha.assets.GameAssets;
import dev.workstho.bruhaha.gameplay.GameSession;

/** 3D felt table backdrop — playable HD cards are Scene2D on top. */
public class TableWorld implements Disposable {
    private final PerspectiveCamera camera;
    private final ModelBatch modelBatch;
    private final Environment environment;
    private final CardModels models;
    private final ModelInstance table;
    private final ModelInstance rail;
    private final ModelInstance deckInstance;

    public TableWorld(GameAssets assets) {
        camera = new PerspectiveCamera(42f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 9.5f, 0.1f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();

        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.55f, 0.55f, 0.58f, 1f));
        environment.add(new DirectionalLight().set(0.9f, 0.88f, 0.82f, -0.2f, -1f, -0.15f));

        models = new CardModels(assets);
        models.create();
        table = models.table();
        rail = models.rail();
        deckInstance = models.back();
        CardModels.place(deckInstance, -5.5f, 0.15f, 0f, 0f, 8f);
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    public void sync(GameSession session) {
        int deckSize = session.getDeck() != null ? session.getDeck().size() : 0;
        CardModels.place(deckInstance, -5.5f, 0.15f + Math.min(deckSize, 24) * 0.008f, 0f, 0f, 8f);
    }

    public void render() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        camera.update();
        modelBatch.begin(camera);
        modelBatch.render(rail, environment);
        modelBatch.render(table, environment);
        modelBatch.render(deckInstance, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        models.dispose();
    }
}
