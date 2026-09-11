package dev.workstho.bruhaha.view3d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import dev.workstho.bruhaha.assets.GameAssets;
import dev.workstho.bruhaha.model.CardType;

/** ModelBuilder card/table meshes textured with designed PNG assets. */
public class CardModels implements Disposable {
    public static final float CARD_W = 1.0f;
    public static final float CARD_H = 1.4f;
    public static final float CARD_D = 0.04f;

    private final GameAssets assets;
    private final ModelBuilder builder = new ModelBuilder();
    private final ObjectMap<CardType, Model> faceModels = new ObjectMap<>();
    private Model backModel;
    private Model tableModel;
    private Model railModel;
    private final Array<Model> owned = new Array<>();

    public CardModels(GameAssets assets) {
        this.assets = assets;
    }

    public void create() {
        long attrs = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
        for (CardType type : CardType.values()) {
            Material mat = new Material(TextureAttribute.createDiffuse(assets.face(type)));
            Model model = builder.createBox(CARD_W, CARD_D, CARD_H, mat, attrs);
            faceModels.put(type, model);
            owned.add(model);
        }
        Material backMat = new Material(TextureAttribute.createDiffuse(assets.back()));
        backModel = builder.createBox(CARD_W, CARD_D, CARD_H, backMat, attrs);
        owned.add(backModel);

        Material tableMat = new Material(ColorAttribute.createDiffuse(new Color(0.14f, 0.36f, 0.28f, 1f)));
        tableModel = builder.createBox(16f, 0.15f, 11f, tableMat, Usage.Position | Usage.Normal);
        owned.add(tableModel);

        Material railMat = new Material(ColorAttribute.createDiffuse(new Color(0.08f, 0.18f, 0.14f, 1f)));
        railModel = builder.createBox(16.5f, 0.4f, 11.5f, railMat, Usage.Position | Usage.Normal);
        owned.add(railModel);
    }

    public ModelInstance face(CardType type) {
        return new ModelInstance(faceModels.get(type));
    }

    public ModelInstance back() {
        return new ModelInstance(backModel);
    }

    public ModelInstance table() {
        ModelInstance i = new ModelInstance(tableModel);
        i.transform.setToTranslation(0f, -0.08f, 0f);
        return i;
    }

    public ModelInstance rail() {
        ModelInstance i = new ModelInstance(railModel);
        i.transform.setToTranslation(0f, -0.3f, 0f);
        return i;
    }

    public static void place(ModelInstance instance, float x, float y, float z, float tiltDeg, float yawDeg) {
        instance.transform.idt();
        instance.transform.translate(x, y, z);
        instance.transform.rotate(Vector3.Y, yawDeg);
        instance.transform.rotate(Vector3.X, tiltDeg);
    }

    @Override
    public void dispose() {
        for (Model m : owned) m.dispose();
        owned.clear();
        faceModels.clear();
    }
}
