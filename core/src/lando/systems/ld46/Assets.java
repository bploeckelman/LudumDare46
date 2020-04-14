package lando.systems.ld46;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.*;

public class Assets implements Disposable {

    private final AssetDescriptor<TextureAtlas> atlasAsset = new AssetDescriptor<>("images/sprites.atlas", TextureAtlas.class);
    private final AssetDescriptor<Texture> pixelTextureAsset = new AssetDescriptor<>("images/pixel.png", Texture.class);

    public enum Loading { SYNC, ASYNC }

    public boolean initialized;
    public SpriteBatch batch;
    public ShapeRenderer shapes;
    public GlyphLayout layout;
    public AssetManager mgr;

    public Texture pixel;

    public TextureRegion debugTexture;
    public TextureRegion whitePixel;
    public TextureRegion whiteCircle;

    public TextureAtlas atlas;

    public Assets() {
        this(Loading.SYNC);
    }

    public Assets(Loading loading) {
        // Let us write shitty shader programs
        ShaderProgram.pedantic = false;

        initialized = false;

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        layout = new GlyphLayout();

        mgr = new AssetManager();
        mgr.load(atlasAsset);
        mgr.load(pixelTextureAsset);

        if (loading == Loading.SYNC) {
            mgr.finishLoading();
            updateLoading();
        }
    }

    public float updateLoading() {
        if (!mgr.update()) return mgr.getProgress();
        if (initialized) return 1f;
        initialized = true;

        pixel = mgr.get(pixelTextureAsset);

        // Cache TextureRegions from TextureAtlas in fields for quicker access
        atlas = mgr.get(atlasAsset);
        debugTexture = atlas.findRegion("badlogic");
        whitePixel = atlas.findRegion("white-pixel");
        whiteCircle = atlas.findRegion("white-circle");

        return 1f;
    }

    @Override
    public void dispose() {
        mgr.clear();
        batch.dispose();
        shapes.dispose();
    }

}
