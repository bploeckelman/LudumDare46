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
import com.badlogic.gdx.utils.I18NBundle;
import lando.systems.ld46.entities.BodyPart;

import javax.xml.soap.Text;

public class Assets implements Disposable {

    private final AssetDescriptor<TextureAtlas> atlasAsset = new AssetDescriptor<>("images/sprites.atlas", TextureAtlas.class);
    private final AssetDescriptor<Texture> pixelTextureAsset = new AssetDescriptor<>("images/pixel.png", Texture.class);
    private final AssetDescriptor<Texture> launchTextureAsset = new AssetDescriptor<>("images/launch.png", Texture.class);
    private final AssetDescriptor<Texture> titleTextureAsset = new AssetDescriptor<>("images/title.png", Texture.class);
    private final AssetDescriptor<BitmapFont> pixelFont16Asset = new AssetDescriptor<>("fonts/emulogic-16pt.fnt", BitmapFont.class);
    private final AssetDescriptor<BitmapFont> riseFont16Asset = new AssetDescriptor<>("fonts/chevyray-rise-16.fnt", BitmapFont.class);

    public enum Loading { SYNC, ASYNC }

    public boolean initialized;
    public SpriteBatch batch;
    public ShapeRenderer shapes;
    public GlyphLayout layout;
    public AssetManager mgr;
    public I18NBundle tutorialText;

    public Texture launchImage;
    public Texture titleImage;
    public Texture pixel;

    public NinePatch debugNinePatch;
    public NinePatch tutorialNinePatch;

    public TextureRegion debugTexture;
    public TextureRegion whitePixel;
    public TextureRegion whiteCircle;
    public TextureRegion punchWall1x4;
    public TextureRegion ringTexture;
    public TextureRegion sunsetBackground;
    public TextureRegion mausoleumBackground;
    public TextureRegion zombieRippedArm;
    public TextureRegion zombieRippedLeg;
    public TextureRegion zombieRippedHead;
    public TextureRegion zombiePin;
    public TextureRegion zombieArmPin;
    public TextureRegion zombieLegPin;
    public TextureRegion zombieHeadPin;
    public TextureRegion iconHeart;
    public TextureRegion iconSkull;
    public TextureRegion iconArrow;
    public TextureRegion smokeTex;
    public TextureRegion particleSparkle;
    public TextureRegion particleBlood1;
    public TextureRegion particleBlood2;
    public TextureRegion particleBlood3;
    public TextureRegion particleBloodSplat1;

    public TextureRegion grave;

    public Animation<TextureRegion> playerAnimation;
    public Animation<TextureRegion> playerMoveAnimation;
    public Animation<TextureRegion> playerAttackAnimation;
    public Animation<TextureRegion> playerJumpAnimation;
    public Animation<TextureRegion> playerFallAnimation;
    public Animation<TextureRegion> playerDieAnimation;

    public Animation<TextureRegion> mechAnimation;
    public Animation<TextureRegion> mechMoveAnimation;
    public Animation<TextureRegion> mechAttackAnimation;
    public Animation<TextureRegion> mechJumpAnimation;
    public Animation<TextureRegion> mechFallAnimation;

    // build
    public Animation<TextureRegion> mechBuildAnimation;
    public Animation<TextureRegion> playerBuildAnimation;

    public Animation<TextureRegion> playerEnterMech;
    public Animation<TextureRegion> playerLeaveMech;

    // enemies!!
    public Animation<TextureRegion> mobBossAnimation;
    public Animation<TextureRegion> mobPitchforkAnimation;
    public Animation<TextureRegion> mobTorchAnimation;

    public Animation<TextureRegion> batAnimation;
    public Animation<TextureRegion> snakeAnimation;
    public Animation<TextureRegion> mummyAnimation;

    public Animation<TextureRegion> bossWalkAnimation;
    public Animation<TextureRegion> bossPunchAnimation;
    public Animation<TextureRegion> bossHurtAnimation;

    // drops - ftw!
    public Animation<TextureRegion> fleshDropAnimation;
    public Animation<TextureRegion> syringeDropAnimation;
    public Animation<TextureRegion> holyHandGrenadeDropAnimation;

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
    public Music barkMusic;
    public Music ritzMusic;

    public Sound sampleSound;
    public Sound doc_punch;
    public Sound doc_punch_land;
    public Sound doc_jump;
    public Sound doc_hurt;
    public Sound doc_death;
    public Sound zombie_punch;
    public Sound zombie_punch_land;
    public Sound zombie_punch_wall;
    public Sound zombie_jump;
    public Sound zombie_hurt;
    public Sound zombie_death;
    public Sound pickup_flesh;
    public Sound pickup_syringe;
    public Sound pickup_handgrenade;
    public Sound bat_attack;
    public Sound bat_hurt;
    public Sound bat_death;
    public Sound snek_attack;
    public Sound snek_hurt;
    public Sound snek_death;
    public Sound mob_boss_flee;


    public BitmapFont pixelFont16;
    public BitmapFont riseFont16;

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
        mgr.load(launchTextureAsset);
        mgr.load(titleTextureAsset);
        mgr.load("i18n/tutorial", I18NBundle.class);

        mgr.load("audio/sample-music.wav", Music.class);
        mgr.load("audio/bark.mp3", Music.class);
        mgr.load("audio/ritz-loop.mp3", Music.class);
        mgr.load("audio/sample-sound.wav", Sound.class);
        mgr.load("audio/doc_punch.wav", Sound.class);
        mgr.load("audio/doc_punch_land.wav", Sound.class);
        mgr.load("audio/doc_jump.wav", Sound.class);
        mgr.load("audio/doc_hurt.wav", Sound.class);
        mgr.load("audio/doc_death.wav", Sound.class);
        mgr.load("audio/zombie_punch.wav", Sound.class);
        mgr.load("audio/zombie_punch_land.wav", Sound.class);
        mgr.load("audio/zombie_punch_wall.wav", Sound.class);
        mgr.load("audio/zombie_jump.wav", Sound.class);
        mgr.load("audio/zombie_hurt.wav", Sound.class);
        mgr.load("audio/zombie_death.wav", Sound.class);
        mgr.load("audio/puckup_flesh.wav", Sound.class);
        mgr.load("audio/pickup_syringe.wav", Sound.class);
        mgr.load("audio/pickup_handgrenade.wav", Sound.class);
        mgr.load("audio/bat_attack.wav", Sound.class);
        mgr.load("audio/batt_hurt.wav", Sound.class);
        mgr.load("audio/bat_death.wav", Sound.class);
        mgr.load("audio/snek_attack.wav", Sound.class);
        mgr.load("audio/snek_hurt.wav", Sound.class);
        mgr.load("audio/snek_death.wav", Sound.class);
        mgr.load("audio/tom_gerry_run_sound.wav", Sound.class);

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
        titleImage = mgr.get(titleTextureAsset);

        tutorialText = mgr.get("i18n/tutorial", I18NBundle.class);

        // Cache TextureRegions from TextureAtlas in fields for quicker access
        atlas = mgr.get(atlasAsset);

        debugNinePatch = new NinePatch(atlas.findRegion("debug_patch"), 6, 6, 6, 6);
        tutorialNinePatch = new NinePatch(atlas.findRegion("ninepatch-screws"), 12, 12, 12, 12);

        debugTexture = atlas.findRegion("badlogic");
        whitePixel = atlas.findRegion("white-pixel");
        whiteCircle = atlas.findRegion("white-circle");
        punchWall1x4 = atlas.findRegion("punch-wall-1x4");
        ringTexture = atlas.findRegion("ring");
        sunsetBackground = atlas.findRegion("background-sunset-columns-reflection");
        mausoleumBackground = atlas.findRegion("mausoleum");
        zombieRippedArm = atlas.findRegion("zombie-ripped-arm");
        zombieRippedHead = atlas.findRegion("zombie-ripped-head");
        zombieRippedLeg = atlas.findRegion("zombie-ripped-leg");
        zombiePin = atlas.findRegion("zombie-pin");
        zombieArmPin = atlas.findRegion("zombie-arm-pin");
        zombieLegPin = atlas.findRegion("zombie-leg-pin");
        zombieHeadPin = atlas.findRegion("zombie-head-pin");
        iconHeart = atlas.findRegion("icon-heart");
        iconSkull = atlas.findRegion("icon-skull");
        iconArrow = atlas.findRegion("icon-arrow");
        smokeTex = atlas.findRegion("smoke");
        particleSparkle = atlas.findRegion("particle-sparkle");
        particleBlood1 = atlas.findRegion("particle-blood-1");
        particleBlood2 = atlas.findRegion("particle-blood-2");
        particleBlood3 = atlas.findRegion("particle-blood-3");
        particleBloodSplat1 = atlas.findRegion("particle-blood-splat-1");

        grave = atlas.findRegion("grave-marker");

        BodyPart.Type.arm1.texture = zombieRippedArm;
        BodyPart.Type.arm2.texture = zombieRippedArm;
        BodyPart.Type.leg1.texture = zombieRippedLeg;
        BodyPart.Type.leg2.texture = zombieRippedLeg;
        BodyPart.Type.head.texture  = zombieRippedHead;

        BodyPart.Type.arm1.pin = zombieArmPin;
        BodyPart.Type.arm2.pin = zombieArmPin;
        BodyPart.Type.leg1.pin = zombieLegPin;
        BodyPart.Type.leg2.pin = zombieLegPin;
        BodyPart.Type.head.pin  = zombieHeadPin;

        playerAnimation = new Animation<>(0.3f, atlas.findRegions("doc-idle"), Animation.PlayMode.LOOP);
        playerMoveAnimation = new Animation<>(0.1f, atlas.findRegions("doc-run"), Animation.PlayMode.LOOP);
        playerAttackAnimation = new Animation<>(0.1f, atlas.findRegions("doc-punch"), Animation.PlayMode.NORMAL);
        playerJumpAnimation = new Animation<>(0.04f, atlas.findRegions("doc-jump"), Animation.PlayMode.NORMAL);
        playerFallAnimation = new Animation<>(0.08f, atlas.findRegions("doc-fall"), Animation.PlayMode.NORMAL);
        playerDieAnimation = new Animation<>(0.1f, atlas.findRegions("doc-death"), Animation.PlayMode.NORMAL);

        playerEnterMech = new Animation<>(0.1f, atlas.findRegions("doc-run"), Animation.PlayMode.NORMAL);
        playerLeaveMech = new Animation<>(0.1f, atlas.findRegions("doc-run"), Animation.PlayMode.REVERSED);

        mechAnimation = new Animation<>(0.2f, atlas.findRegions("zombie-idle"), Animation.PlayMode.LOOP);
        mechMoveAnimation = new Animation<>(0.1f, atlas.findRegions("zombie-walk"), Animation.PlayMode.LOOP);
        mechAttackAnimation = new Animation<>(0.15f, atlas.findRegions("zombie-punch"), Animation.PlayMode.NORMAL);
        mechJumpAnimation = new Animation<>(0.06f, atlas.findRegions("zombie-jump"), Animation.PlayMode.NORMAL);
        mechFallAnimation = new Animation<>(0.5f, atlas.findRegions("zombie-fall"), Animation.PlayMode.NORMAL);

        mechBuildAnimation = new Animation<>(0.2f, atlas.findRegions("zombie-rise"), Animation.PlayMode.NORMAL);
        playerBuildAnimation = new Animation<>(0.1f, atlas.findRegions("doc-build"), Animation.PlayMode.LOOP);

        mobBossAnimation = new Animation<>(0.2f, atlas.findRegions("organ-grinder"), Animation.PlayMode.LOOP);
        mobPitchforkAnimation = new Animation<>(0.1f, atlas.findRegions("pitchfork-idle"), Animation.PlayMode.LOOP);
        mobTorchAnimation = new Animation<>(0.3f, atlas.findRegions("torch-idle"), Animation.PlayMode.LOOP);

        bossWalkAnimation = new Animation<>(0.3f, atlas.findRegions("boss-walk"), Animation.PlayMode.LOOP);
        bossPunchAnimation = new Animation<>(0.2f, atlas.findRegions("boss-punch"), Animation.PlayMode.NORMAL);
        bossHurtAnimation = new Animation<>(0.2f, atlas.findRegions("boss-hurt"), Animation.PlayMode.NORMAL);

        batAnimation = new Animation<>(0.1f, atlas.findRegions("bat"), Animation.PlayMode.LOOP);
        snakeAnimation = new Animation<>(0.1f, atlas.findRegions("snake"), Animation.PlayMode.LOOP);
        mummyAnimation = new Animation<>(0.1f, atlas.findRegions("mummy-walk"), Animation.PlayMode.LOOP);

        fleshDropAnimation = new Animation<>(0.3f, atlas.findRegions("pickup-meat"), Animation.PlayMode.LOOP);
        syringeDropAnimation = new Animation<>(0.3f, atlas.findRegions("pickup-syringe"), Animation.PlayMode.LOOP);
        holyHandGrenadeDropAnimation = new Animation<>(0.1f, atlas.findRegions("pickup-holyhand"), Animation.PlayMode.LOOP);

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
        doc_punch = mgr.get("audio/doc_punch.wav", Sound.class);
        doc_punch_land = mgr.get("audio/doc_punch_land.wav", Sound.class);
        doc_jump = mgr.get("audio/doc_jump.wav", Sound.class);
        doc_hurt = mgr.get("audio/doc_hurt.wav", Sound.class);
        doc_death = mgr.get("audio/doc_death.wav", Sound.class);
        zombie_punch = mgr.get("audio/zombie_punch.wav", Sound.class);
        zombie_punch_land = mgr.get("audio/zombie_punch_land.wav", Sound.class);
        zombie_punch_wall = mgr.get("audio/zombie_punch_wall.wav", Sound.class);
        zombie_jump = mgr.get("audio/zombie_jump.wav", Sound.class);
        zombie_hurt = mgr.get("audio/zombie_hurt.wav", Sound.class);
        zombie_death = mgr.get("audio/zombie_death.wav", Sound.class);
        pickup_flesh = mgr.get("audio/puckup_flesh.wav", Sound.class);
        pickup_syringe = mgr.get("audio/pickup_syringe.wav", Sound.class);
        pickup_handgrenade = mgr.get("audio/pickup_handgrenade.wav", Sound.class);
        bat_attack = mgr.get("audio/bat_attack.wav", Sound.class);
        bat_hurt = mgr.get("audio/batt_hurt.wav", Sound.class);
        bat_death = mgr.get("audio/bat_death.wav", Sound.class);
        snek_attack = mgr.get("audio/snek_attack.wav", Sound.class);
        snek_hurt = mgr.get("audio/snek_hurt.wav", Sound.class);
        snek_death = mgr.get("audio/snek_death.wav", Sound.class);
        mob_boss_flee = mgr.get("audio/tom_gerry_run_sound.wav", Sound.class);

        sampleMusic = mgr.get("audio/sample-music.wav", Music.class);
        barkMusic = mgr.get("audio/bark.mp3", Music.class);
        ritzMusic = mgr.get("audio/ritz-loop.mp3", Music.class);

        pixelFont16 = mgr.get(pixelFont16Asset);
        pixelFont16.getData().markupEnabled = true;
        riseFont16 = mgr.get(riseFont16Asset);
        riseFont16.getData().markupEnabled = true;

        return 1f;
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
