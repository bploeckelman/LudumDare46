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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.ld46.world.Level;
import lando.systems.ld46.world.LevelDescriptor;

public class Assets implements Disposable {

    private final AssetDescriptor<TextureAtlas> atlasAsset = new AssetDescriptor<>("images/sprites.atlas", TextureAtlas.class);
    private final AssetDescriptor<Texture> pixelTextureAsset = new AssetDescriptor<>("images/pixel.png", Texture.class);
    private final AssetDescriptor<Texture> launchTextureAsset = new AssetDescriptor<>("images/launch.png", Texture.class);
    private final AssetDescriptor<BitmapFont> pixelFont16Asset = new AssetDescriptor<>("fonts/emulogic-16pt.fnt", BitmapFont.class);
    private final AssetDescriptor<BitmapFont> riseFont16Asset = new AssetDescriptor<>("fonts/chevyray-rise-16.fnt", BitmapFont.class);

    public enum Loading { SYNC, ASYNC }

    public boolean initialized;
    public SpriteBatch batch;
    public ShapeRenderer shapes;
    public GlyphLayout layout;
    public AssetManager mgr;

    public Texture launchImage;
    public Texture pixel;

    public NinePatch debugNinePatch;

    public TextureRegion debugTexture;
    public TextureRegion whitePixel;
    public TextureRegion whiteCircle;
    public TextureRegion ringTexture;

    public Animation<TextureRegion> playerAnimation;
    public Animation<TextureRegion> playerMoveAnimation;
    public Animation<TextureRegion> playerAttackAnimation;

    public Animation<TextureRegion> mechAnimation;

    public Array<ShaderProgram> randomTransitions;
    public ShaderProgram blindsShader;
    public ShaderProgram fadeShader;
    public ShaderProgram radialShader;
    public ShaderProgram doomShader;
    public ShaderProgram pizelizeShader;
    public ShaderProgram doorwayShader;
    public ShaderProgram crosshatchShader;
    public ShaderProgram rippleShader;
    public ShaderProgram heartShader;
    public ShaderProgram stereoShader;
    public ShaderProgram circleCropShader;
    public ShaderProgram cubeShader;
    public ShaderProgram dreamyShader;

    public TextureAtlas atlas;

    public Music sampleMusic;

    public Sound sampleSound;

    public BitmapFont pixelFont16;
    public BitmapFont riseFont16;

    public ObjectMap<LevelDescriptor, Level> levels;

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
        levels = new ObjectMap<>();

        mgr = new AssetManager();
        mgr.load(atlasAsset);
        mgr.load(pixelTextureAsset);
        mgr.load(launchTextureAsset);

        mgr.load("audio/sample-music.wav", Music.class);
        mgr.load("audio/sample-sound.wav", Sound.class);

        mgr.load(pixelFont16Asset);
        mgr.load(riseFont16Asset);

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
        launchImage = mgr.get(launchTextureAsset);

        // Cache TextureRegions from TextureAtlas in fields for quicker access
        atlas = mgr.get(atlasAsset);

        debugNinePatch = new NinePatch(atlas.findRegion("debug_patch"), 6, 6, 6, 6);

        debugTexture = atlas.findRegion("badlogic");
        whitePixel = atlas.findRegion("white-pixel");
        whiteCircle = atlas.findRegion("white-circle");
        ringTexture = atlas.findRegion("ring");

        playerAnimation = new Animation<>(0.3f, atlas.findRegions("doc-idle"), Animation.PlayMode.LOOP);
        playerMoveAnimation = new Animation<>(0.1f, atlas.findRegions("doc-run"), Animation.PlayMode.LOOP);
        playerAttackAnimation = new Animation<>(0.1f, atlas.findRegions("doc-punch"), Animation.PlayMode.NORMAL);
;
        mechAnimation = new Animation<>(0.1f, atlas.findRegions("zombie-test"), Animation.PlayMode.LOOP);

        randomTransitions = new Array<>();
        blindsShader = loadShader("shaders/default.vert", "shaders/blinds.frag");
        fadeShader = loadShader("shaders/default.vert", "shaders/dissolve.frag");
        radialShader = loadShader("shaders/default.vert", "shaders/radial.frag");
        doomShader = loadShader("shaders/default.vert", "shaders/doomdrip.frag");
        pizelizeShader = loadShader("shaders/default.vert", "shaders/pixelize.frag");
        doorwayShader = loadShader("shaders/default.vert", "shaders/doorway.frag");
        crosshatchShader = loadShader("shaders/default.vert", "shaders/crosshatch.frag");
        rippleShader = loadShader("shaders/default.vert", "shaders/ripple.frag");
        heartShader = loadShader("shaders/default.vert", "shaders/heart.frag");
        stereoShader = loadShader("shaders/default.vert", "shaders/stereo.frag");
        circleCropShader = loadShader("shaders/default.vert", "shaders/circlecrop.frag");
        cubeShader = loadShader("shaders/default.vert", "shaders/cube.frag");
        dreamyShader = loadShader("shaders/default.vert", "shaders/dreamy.frag");

        randomTransitions.add(radialShader);

        sampleSound = mgr.get("audio/sample-sound.wav", Sound.class);
        sampleMusic = mgr.get("audio/sample-music.wav", Music.class);

        pixelFont16 = mgr.get(pixelFont16Asset);
        pixelFont16.getData().markupEnabled = true;
        riseFont16 = mgr.get(riseFont16Asset);
        riseFont16.getData().markupEnabled = true;

        return 1f;
    }

    public void preloadLevels() {
        for (LevelDescriptor levelDescriptor : LevelDescriptor.values()) {
            levels.put(levelDescriptor, new Level(levelDescriptor, Game.game));
        }
    }

    private static ShaderProgram loadShader(String vertSourcePath, String fragSourcePath) {
        ShaderProgram.pedantic = false;
        ShaderProgram shaderProgram = new ShaderProgram(
                Gdx.files.internal(vertSourcePath),
                Gdx.files.internal(fragSourcePath));

        if (!shaderProgram.isCompiled()) {
            Gdx.app.error("LoadShader", "compilation failed:\n" + shaderProgram.getLog());
            throw new GdxRuntimeException("LoadShader: compilation failed:\n" + shaderProgram.getLog());
        } else if (Config.shaderDebug){
            Gdx.app.setLogLevel(Gdx.app.LOG_DEBUG);
            Gdx.app.debug("LoadShader", "ShaderProgram compilation log: " + shaderProgram.getLog());
        }

        return shaderProgram;
    }

    @Override
    public void dispose() {
        mgr.clear();
        batch.dispose();
        shapes.dispose();
    }

}
