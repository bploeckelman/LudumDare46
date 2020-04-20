package lando.systems.ld46.screens;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld46.Config;
import lando.systems.ld46.Game;
import lando.systems.ld46.backgrounds.ParallaxBackground;
import lando.systems.ld46.backgrounds.ParallaxUtils;
import lando.systems.ld46.backgrounds.TextureRegionParallaxLayer;
import lando.systems.ld46.entities.*;
import lando.systems.ld46.particles.Particles;
import lando.systems.ld46.physics.PhysicsComponent;
import lando.systems.ld46.physics.PhysicsSystem;
import lando.systems.ld46.ui.tutorial.TutorialManager;
import lando.systems.ld46.world.Level;
import lando.systems.ld46.world.LevelDescriptor;

public class GameScreen extends BaseScreen {

    public Player player;
    public Level level;

    private Vector3 touchPos;

    private static final float MAX_ZOOM = 2f;
    private static final float MIN_ZOOM = 0.1f;
    private static final float ZOOM_LERP = 0.02f;
    private static final float PAN_LERP = 0.1f;
    private static final float CAM_HORZ_MARGIN = 100;
    private static final float CAM_VERT_MARGIN = 20;
    private static final float CAM_VERT_JUMP_MARGIN = 150;

    public Vector3 cameraTargetPos;
    private MutableFloat targetZoom = new MutableFloat(1.0f);
    private boolean cameraOverride = false;

    private ParallaxBackground background;
    public TutorialManager tutorials;

    public ZombieMech zombieMech;
    public PhysicsSystem physicsSystem;
    public Array<PhysicsComponent> physicsEntities;

    public Array<EnemyEntity> enemies;
    public Array<DropEntity> drops;

    public BodyBag bodyBag;
    public Animation<TextureRegion> zombieMechBuildAnimation;
    public float zombieMechBuildAnimTime;
    public boolean buildingMech;

    public GameScreen(Game game) {
        super(game);
        this.touchPos = new Vector3();
        this.zombieMechBuildAnimation = assets.mechBuildAnimation;

        loadLevel(LevelDescriptor.level_tutorial);
    }

    public void loadLevel(LevelDescriptor levelDescriptor) {
        this.level = new Level(levelDescriptor, this);
        this.physicsSystem = new PhysicsSystem(this);
        this.physicsEntities = new Array<>();
        this.player = new Player(this, level.playerSpawn);
        this.zombieMech = null;
        this.enemies = new Array<>();
        this.drops = new Array<>();
        this.cameraTargetPos = new Vector3(player.imageBounds.x + player.imageBounds.width / 2f, player.imageBounds.y + player.imageBounds.height / 2f, 0f);
        this.worldCamera.position.set(cameraTargetPos);
        TiledMapTileLayer collisionLayer = level.layers.get(Level.LayerType.collision).tileLayer;
        float levelHeight = collisionLayer.getHeight() * collisionLayer.getTileHeight();
        this.background = new ParallaxBackground(new TextureRegionParallaxLayer(assets.sunsetBackground, levelHeight, new Vector2(.5f, .9f), ParallaxUtils.WH.height));
        this.bodyBag = new BodyBag(this, level.initialBodyPartPositions);
        this.zombieMechBuildAnimTime = 0f;
        this.buildingMech = false;
        // TODO: spawn the appropriate tutorial shit for whichever level this is
        this.tutorials = new TutorialManager(this);

        this.physicsEntities.add(player);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(worldCamera.combined);
        {
            batch.begin();
            {
                background.render(worldCamera, batch);
            }
            batch.end();

            level.render(Level.LayerType.background, worldCamera);
            level.render(Level.LayerType.collision, worldCamera);
            batch.begin();
            {
                for (EnemyEntity enemy : enemies) {
                    enemy.render(batch);
                }
                for (DropEntity drop : drops) {
                    drop.render(batch);
                }

                if (zombieMech != null) {
                    zombieMech.render(batch);
                }
                player.render(batch);
                level.renderObjects(batch);
                bodyBag.render(batch);
                if (buildingMech) {
                    batch.draw(zombieMechBuildAnimation.getKeyFrame(zombieMechBuildAnimTime),
                            player.collisionBounds.x + player.collisionBounds.width / 2f, player.collisionBounds.y);
                }
                particles.draw(batch, Particles.Layer.foreground);
            }
            batch.end();
            level.render(Level.LayerType.foreground, worldCamera);
            if (Config.debug) {
                batch.begin();
                {
                    level.renderDebug(batch);
                }
                batch.end();
            }
        }

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        {
            if (Config.debug) {
                assets.pixelFont16.draw(batch, " fps: " + Gdx.graphics.getFramesPerSecond(), 10f, hudCamera.viewportHeight - 10f);
            }
            tutorials.render(batch);
        }
        batch.end();
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if (Gdx.app.getType() == Application.ApplicationType.Desktop && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        handleDebugCommands();
        tutorials.update(dt);

        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        worldCamera.unproject(touchPos);
        particles.update(dt);

        if (tutorials.shouldBlockInput()) return;
        level.update(dt);
        player.update(dt);
        if (level.exit != null && level.nextLevel != null) {
            if (player.collisionBounds.overlaps(level.exit.bounds)
             || (zombieMech != null && zombieMech.collisionBounds.overlaps(level.exit.bounds))) {
                loadLevel(level.nextLevel);
            }
        }

        if (zombieMech != null) {
            zombieMech.update(dt);
            if (zombieMech.dead) {
                player.jumpOut();
                zombieMech = null;
            }
        }

        bodyBag.update(dt, player);
        if (buildingMech) {
            zombieMechBuildAnimTime += dt;
        }

        for (EnemyEntity enemy : enemies) {
            enemy.update(dt);
        }

        for (DropEntity drop : drops) {
            drop.update(dt);
        }

        physicsSystem.update(dt);
        handleCameraConstraints();
    }

    public void buildZombieMech() {
        // hide and block input from player
        player.hide = true;
        player.freeze = true;

        // play zombie build animation right where player was standing
        buildingMech = true;
        zombieMechBuildAnimTime = 0f;

        // when done, spawn mech, reshow and reenable input for player
        Timeline.createSequence()
                .delay(zombieMechBuildAnimation.getAnimationDuration())
                .push(Tween.call((type, source) -> {
                    buildingMech = false;
                    zombieMech = new ZombieMech(GameScreen.this,
                            player.collisionBounds.x + player.collisionBounds.width / 2f, player.collisionBounds.y);
                    physicsEntities.add(zombieMech);
                    player.hide = false;
                    player.freeze = false;
                }))
                .start(game.tween);
    }

    private void handleDebugCommands() {
        if (Config.debug) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                // kill em all, let the engine sort it out
                for (EnemyEntity e : enemies) {
                    e.takeDamage(e.hitPoints);
                }
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                particles.makePhysicsParticles(touchPos.x, touchPos.y);
            }
        }
    }

    private void updateCamera() {
        if (cameraOverride) return;

        worldCamera.zoom = MathUtils.lerp(worldCamera.zoom, targetZoom.floatValue(), ZOOM_LERP);
        worldCamera.zoom = MathUtils.clamp(worldCamera.zoom, MIN_ZOOM, MAX_ZOOM);

        worldCamera.position.x = MathUtils.lerp(worldCamera.position.x, cameraTargetPos.x, PAN_LERP);
        worldCamera.position.y = MathUtils.lerp(worldCamera.position.y, cameraTargetPos.y, PAN_LERP);
        worldCamera.update();
    }

    private void handleCameraConstraints() {
        float playerX = player.position.x + player.collisionBounds.width / 2f;
        if (playerX < cameraTargetPos.x - CAM_HORZ_MARGIN) cameraTargetPos.x = playerX + CAM_HORZ_MARGIN;
        if (playerX > cameraTargetPos.x + CAM_HORZ_MARGIN) cameraTargetPos.x = playerX - CAM_HORZ_MARGIN;

        float playerY = player.position.y + player.collisionBounds.height / 2f;
        if (playerY < cameraTargetPos.y - CAM_VERT_MARGIN) cameraTargetPos.y = playerY + CAM_VERT_MARGIN;
        if (player.grounded) {
            if (playerY > cameraTargetPos.y + CAM_VERT_MARGIN) cameraTargetPos.y = playerY - CAM_VERT_MARGIN;
        } else {
            if (playerY > cameraTargetPos.y + CAM_VERT_JUMP_MARGIN) cameraTargetPos.y = playerY - CAM_VERT_JUMP_MARGIN;
        }

        TiledMapTileLayer collisionTileLayer = level.layers.get(Level.LayerType.collision).tileLayer;
        float collisionLayerWidth      = collisionTileLayer.getWidth();
        float collisionLayerHeight     = collisionTileLayer.getHeight();
        float collisionLayerTileWidth  = collisionTileLayer.getTileWidth();
        float collisionLayerTileHeight = collisionTileLayer.getTileHeight();

        float cameraLeftEdge = worldCamera.viewportWidth / 2f;
        cameraTargetPos.x = MathUtils.clamp(cameraTargetPos.x, cameraLeftEdge, collisionLayerWidth * collisionLayerTileWidth - cameraLeftEdge);

        float cameraVertEdge = worldCamera.viewportHeight / 2f;
        cameraTargetPos.y = MathUtils.clamp(cameraTargetPos.y, cameraVertEdge, collisionLayerHeight * collisionLayerTileHeight - cameraVertEdge);

//        targetZoom.setValue(1 + Math.abs(player.velocity.y / 2000f));

        updateCamera();
    }

}
