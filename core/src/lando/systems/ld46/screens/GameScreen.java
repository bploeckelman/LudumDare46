package lando.systems.ld46.screens;

import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import lando.systems.ld46.entities.EnemyEntity;
import lando.systems.ld46.entities.Player;
import lando.systems.ld46.entities.ZombieMech;
import lando.systems.ld46.particles.Particles;
import lando.systems.ld46.physics.PhysicsComponent;
import lando.systems.ld46.physics.PhysicsSystem;
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

    private Vector3 cameraTargetPos;
    private MutableFloat targetZoom = new MutableFloat(1.0f);
    private boolean cameraOverride = false;

    private ParallaxBackground background;

    public ZombieMech zombieMech;
    public PhysicsSystem physicsSystem;
    public Array<PhysicsComponent> physicsEntities;

    public Array<EnemyEntity> enemies;

    public GameScreen(Game game) {
        super(game);

        this.level = new Level(LevelDescriptor.level1, this);
        this.physicsSystem = new PhysicsSystem(this);
        this.physicsEntities = new Array<>();
        this.player = new Player(this, level.playerSpawn);
        physicsEntities.add(player);
        this.zombieMech = new ZombieMech(this, 400, 500);
        physicsEntities.add(zombieMech);
        this.touchPos = new Vector3();
        this.cameraTargetPos = new Vector3(player.imageBounds.x + player.imageBounds.width / 2f, player.imageBounds.y + player.imageBounds.height / 2f, 0f);

        this.enemies = new Array<>();

        TiledMapTileLayer collisionLayer = level.layers.get(Level.LayerType.collision).tileLayer;
        float levelHeight = collisionLayer.getHeight() * collisionLayer.getTileHeight();
        TextureRegionParallaxLayer sunsetLayer = new TextureRegionParallaxLayer(assets.sunsetBackground, levelHeight, new Vector2(.5f, .9f), ParallaxUtils.WH.height);
        TextureRegionParallaxLayer columnLayer = new TextureRegionParallaxLayer(assets.columnsBackground, levelHeight, new Vector2(.4f, .9f), ParallaxUtils.WH.height);
        // TODO: fix optional padding on parallax layers
        this.background = new ParallaxBackground(sunsetLayer, columnLayer);
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

                player.render(batch);
                zombieMech.render(batch);

                level.renderObjects(batch);

                particles.draw(batch, Particles.Layer.foreground);
                for (EnemyEntity enemy : enemies) {
                    enemy.renderHealthMeter(batch);
                }
                zombieMech.renderHealthMeter(batch);
                if (!player.inMech()) {
                    player.renderHealthMeter(batch);
                }
            }
            batch.end();
            level.render(Level.LayerType.foreground, worldCamera);

            if (Config.debug) {
                batch.begin();
                {
                    level.renderDebug(batch);
//                    physicsSystem.collisionTree.renderDebug(batch);
                }
                batch.end();
            }
        }

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        {
            assets.pixelFont16.draw(batch, " fps: " + Gdx.graphics.getFramesPerSecond(), 10f, hudCamera.viewportHeight - 10f);
        }
        batch.end();
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if (Gdx.app.getType() == Application.ApplicationType.Desktop && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        handleTempCommands();

        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        worldCamera.unproject(touchPos);
        particles.update(dt);

        level.update(dt);

        player.update(dt);
        zombieMech.update(dt);

        for (EnemyEntity enemy : enemies) {
            enemy.update(dt);
        }

        physicsSystem.update(dt);
        handleCameraConstraints();
    }

    private void handleTempCommands() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            // kill em all, let the engine sort it out
            for (EnemyEntity e : enemies) {
                e.takeDamage(e.hitPoints);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)){
            particles.makePhysicsParticles(touchPos.x, touchPos.y);
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
