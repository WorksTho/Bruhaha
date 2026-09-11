package dev.workstho.bruhaha.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import dev.workstho.bruhaha.model.CardType;

/** AssetManager + HD FreeType game fonts (Luckiest Guy / Bangers / Boogaloo). */
public class GameAssets implements Disposable {
    public static final String LOGO = "workstho-games-logo.png";
    public static final String FONT_TITLE = "fonts/LuckiestGuy-Regular.ttf";
    public static final String FONT_STATUS = "fonts/Bangers-Regular.ttf";
    public static final String FONT_HUD = "fonts/Boogaloo-Regular.ttf";
    public static final String CARD_BACK = "cards/back.png";

    private final AssetManager manager = new AssetManager();
    private final ObjectMap<CardType, Texture> faces = new ObjectMap<>();
    private Texture back;
    private Texture logo;

    private BitmapFont titleFont;
    private BitmapFont statusFont;
    private BitmapFont hudFont;
    private BitmapFont buttonFont;
    private BitmapFont scoreFont;

    public void queue() {
        manager.load(LOGO, Texture.class);
        manager.load(CARD_BACK, Texture.class);
        for (CardType type : CardType.values()) {
            manager.load(type.texturePath, Texture.class);
        }
    }

    public boolean update() {
        return manager.update();
    }

    public float getProgress() {
        return manager.getProgress();
    }

    public void finishLoading() {
        manager.finishLoading();
        logo = manager.get(LOGO, Texture.class);
        logo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        back = manager.get(CARD_BACK, Texture.class);
        configureCardTexture(back);
        for (CardType type : CardType.values()) {
            Texture tex = manager.get(type.texturePath, Texture.class);
            configureCardTexture(tex);
            faces.put(type, tex);
        }
        createFonts();
    }

    private void configureCardTexture(Texture tex) {
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        tex.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        tex.setAnisotropicFilter(16f);
    }

    private void createFonts() {
        // Large FreeType sizes = crisp HD UI (not VisUI's tiny baked skin font)
        titleFont = buildFont(FONT_TITLE, 78, 4.5f, new Color(0.05f, 0.05f, 0.08f, 1f));
        statusFont = buildFont(FONT_STATUS, 52, 3.5f, new Color(0.05f, 0.05f, 0.08f, 1f));
        hudFont = buildFont(FONT_HUD, 40, 2.8f, new Color(0.05f, 0.05f, 0.08f, 1f));
        scoreFont = buildFont(FONT_HUD, 44, 3f, new Color(0.05f, 0.05f, 0.08f, 1f));
        buttonFont = buildFont(FONT_STATUS, 38, 3f, new Color(0.08f, 0.02f, 0.05f, 1f));
    }

    private BitmapFont buildFont(String path, int size, float border, Color borderColor) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(path));
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size = size;
        p.color = Color.WHITE;
        p.borderWidth = border;
        p.borderColor = borderColor;
        p.borderStraight = false;
        p.shadowOffsetX = 2;
        p.shadowOffsetY = 2;
        p.shadowColor = new Color(0f, 0f, 0f, 0.45f);
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS
            + "—–…✓★•";
        BitmapFont font = generator.generateFont(p);
        font.setUseIntegerPositions(false);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        generator.dispose();
        return font;
    }

    public Texture face(CardType type) { return faces.get(type); }
    public Texture back() { return back; }
    public Texture logo() { return logo; }
    public BitmapFont titleFont() { return titleFont; }
    public BitmapFont statusFont() { return statusFont; }
    public BitmapFont hudFont() { return hudFont; }
    public BitmapFont scoreFont() { return scoreFont; }
    public BitmapFont buttonFont() { return buttonFont; }

    /** @deprecated use {@link #statusFont()} */
    public BitmapFont smallFont() { return hudFont; }

    public AssetManager getManager() { return manager; }

    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (statusFont != null) statusFont.dispose();
        if (hudFont != null) hudFont.dispose();
        if (scoreFont != null) scoreFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        manager.dispose();
    }
}
