package dev.workstho.bruhaha.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import dev.workstho.bruhaha.model.CardType;

/** Central AssetManager wrapper for card art, fonts, and logo. */
public class GameAssets implements Disposable {
    public static final String LOGO = "workstho-games-logo.png";
    public static final String FONT = "fonts/Anton-Regular.ttf";
    public static final String CARD_BACK = "cards/back.png";

    private final AssetManager manager = new AssetManager();
    private final ObjectMap<CardType, Texture> faces = new ObjectMap<>();
    private Texture back;
    private Texture logo;
    private BitmapFont titleFont;
    private BitmapFont hudFont;
    private BitmapFont smallFont;
    private boolean fontsReady;

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
        titleFont = buildFont(54);
        hudFont = buildFont(32);
        smallFont = buildFont(22);
        fontsReady = true;
    }

    private BitmapFont buildFont(int size) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            com.badlogic.gdx.Gdx.files.internal(FONT));
        FreeTypeFontParameter p = new FreeTypeFontParameter();
        p.size = size;
        p.color = Color.WHITE;
        p.borderWidth = 2.4f;
        p.borderColor = Color.BLACK;
        p.borderStraight = true;
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        BitmapFont font = generator.generateFont(p);
        font.setUseIntegerPositions(true);
        generator.dispose();
        return font;
    }

    public Texture face(CardType type) { return faces.get(type); }
    public Texture back() { return back; }
    public Texture logo() { return logo; }
    public BitmapFont titleFont() { return titleFont; }
    public BitmapFont hudFont() { return hudFont; }
    public BitmapFont smallFont() { return smallFont; }
    public boolean isFontsReady() { return fontsReady; }
    public AssetManager getManager() { return manager; }

    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (hudFont != null) hudFont.dispose();
        if (smallFont != null) smallFont.dispose();
        manager.dispose();
    }
}
