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
import lando.systems.ld46.Audio;
import lando.systems.ld46.Config;
import lando.systems.ld46.Game;
import lando.systems.ld46.backgrounds.ParallaxBackground;
import lando.systems.ld46.backgrounds.ParallaxUtils;
import lando.systems.ld46.backgrounds.TextureRegionParallaxLayer;
import lando.systems.ld46.entities.*;
import lando.systems.ld46.entities.boss.Boss;
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

    public Boss boss;
    public Array<EnemyEntity> enemies;
    public Array<DropEntity> drops;
    public Array<GameEntity> randomShit;

    public BodyBag bodyBag;
    public float zombieMechBuildAnimTime;
    public float zombieMechBuildParticleSpawnTimer;
    public boolean buildingMech;
    public float climbAnimTime;
    public boolean climbIn;
    public boolean climbOut;

    public GameScreen(Game game) {
        super(game);
        this.touchPos = new Vector3();
        loadLevel(LevelDescriptor.level_tutorial);
//        loadLevel(LevelDescriptor.level_boss);
    }

    public void loadLevel(LevelDescriptor levelDescriptor) {
        this.level = new Level(levelDescriptor, this);
        this.physicsSystem = new PhysicsSystem(this);
        this.physicsEntities = new Array<>();
        this.player = new Player(this, level.playerSpawn);
        this.zombieMech = null;
        this.enemies = new Array<>();
        this.drops = new Array<>();
        this.randomShit = new Array<>();
        this.cameraTargetPos = new Vector3(player.imageBounds.x + player.imageBounds.width / 2f, player.imageBounds.y + player.imageBounds.height / 2f, 0f);
        this.worldCamera.position.set(cameraTargetPos);
        TiledMapTileLayer collisionLayer = level.layers.get(Level.LayerType.collision).tileLayer;
        float levelHeight = collisionLayer.getHeight() * collisionLayer.getTileHeight();
        if (levelDescriptor == LevelDescriptor.level_boss){
            this.background = new ParallaxBackground(new TextureRegionParallaxLayer(assets.mausoleumBackground, levelHeight, new Vector2(.5f, .9f), ParallaxUtils.WH.height));
        } else {
            this.background = new ParallaxBackground(new TextureRegionParallaxLayer(assets.sunsetBackground, levelHeight, new Vector2(.5f, .9f), ParallaxUtils.WH.height));
        }
        this.bodyBag = new BodyBag(this, level.initialBodyPartPositions);
        this.zombieMechBuildAnimTime = 0f;
        this.climbAnimTime = 0f;
        this.climbIn = this.climbOut = false;
        this.buildingMech = false;
        // TODO: spawn the appropriate tutorial shit for whichever level this is
        this.tutorials = new TutorialManager(this);

        this.physicsEntities.add(player);
        game.audio.fadeMusic(Audio.Musics.ritzMusic);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(worldCamera.combined);
        {
            batch.begin();
            {
                background.render(worldCamera, batch);
                // Fuck it, we'll do it live
                if (level.thisLevel == LevelDescriptor.level_tutorial) {
                    batch.draw(assets.mausoleumBackground, 64f, 64f);
                }
                if (boss != null) boss.render(batch);
            }
            batch.end();

            level.render(Level.LayerType.background, worldCamera);
            level.render(Level.LayerType.collision, worldCamera);
            batch.begin();
            {
                for (GameEntity entity : randomShit) {
                    entity.render(batch);
                }
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
                    renderMechBuild();
                } else if (climbIn) {
                    renderClimbIn();
                } else if (climbOut) {
                    renderClimbOut();
                }

                particles.draw(batch, Particles.Layer.foreground);


            }
            batch.end();
            level.render(Level.LayerType.foreground, worldCamera);
            batch.begin();
            {
                // fhese are already drawn on the entity - stop adding back in
//                if (zombieMech != null) {
//                    zombieMech.renderHealthMeter(batch);
//                }
//                if (!player.inMech() && !player.hide) {
//                    player.renderHealthMeter(batch);
//                }
                if (!player.inMech() && zombieMech != null) {
                    zombieMech.mechIndicator.render(batch);
                }
                for (BodyPart part : bodyBag.bodyParts.values()) {
                    part.renderBodyPartPins(batch);
                }
            }
            batch.end();
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

    private void renderMechBuild() {
        TextureRegion frame = assets.mechBuildAnimation.getKeyFrame(zombieMechBuildAnimTime);
        float x = player.position.x;
        float y = player.collisionBounds.y;

        batch.draw(frame, x - frame.getRegionWidth(), y, frame.getRegionWidth()/2, frame.getRegionHeight()/2,
                frame.getRegionWidth() * ZombieMech.SCALE, frame.getRegionHeight() * ZombieMech.SCALE, 1, 1, 0);

        particles.draw(batch, Particles.Layer.middle);

        frame = assets.playerBuildAnimation.getKeyFrame(zombieMechBuildAnimTime);
        batch.draw(frame, x - frame.getRegionWidth(), y, frame.getRegionWidth()/2, frame.getRegionHeight()/2,
                frame.getRegionWidth() * Player.SCALE, frame.getRegionHeight() * Player.SCALE, 1, 1, 0);

    }

    public void renderClimbIn() {

        TextureRegion frame = assets.playerEnterMech.getKeyFrame(climbAnimTime);
        float x = player.position.x;
        float y = player.collisionBounds.y;

        batch.draw(frame, x - frame.getRegionWidth(), y, frame.getRegionWidth()/2, frame.getRegionHeight()/2,
                frame.getRegionWidth() * ZombieMech.SCALE, frame.getRegionHeight() * ZombieMech.SCALE, 1, 1, 0);

    }

    public void renderClimbOut() {
        TextureRegion frame = assets.playerLeaveMech.getKeyFrame(climbAnimTime);
        float x = player.position.x;
        float y = player.collisionBounds.y;

        batch.draw(frame, x - frame.getRegionWidth(), y, frame.getRegionWidth()/2, frame.getRegionHeight()/2,
                frame.getRegionWidth() * ZombieMech.SCALE, frame.getRegionHeight() * ZombieMech.SCALE, 1, 1, 0);

    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if (Gdx.app.getType() == Application.ApplicationType.Desktop && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) && level.nextLevel == null){
            game.setScreen(new EndScreen(game), assets.cubeShader, 3f);
        }

        handleDebugCommands();
        tutorials.update(dt);

        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        worldCamera.unproject(touchPos);
        particles.update(dt);

        if (tutorials.shouldBlockInput()) return;
        if (boss != null) boss.update(dt);
        level.update(dt);

        for (GameEntity entity : randomShit) {
            entity.update(dt);
        }

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

        if (!player.inMech() && zombieMech != null) {
            zombieMech.mechIndicator.show = true;
            zombieMech.mechIndicator.setTargetPosition(zombieMech.position.x, zombieMech.position.y);
            zombieMech.mechIndicator.update(dt);
        } else if (player.inMech() && zombieMech != null) {
            zombieMech.mechIndicator.show = false;
        }

        bodyBag.update(dt, player);
        if (buildingMech) {
            zombieMechBuildAnimTime += dt;
            zombieMechBuildParticleSpawnTimer += dt;
            if (zombieMechBuildParticleSpawnTimer > 0.33f) {
                zombieMechBuildParticleSpawnTimer = 0f;
                particles.makeZombieBuildClouds(player.collisionBounds.x + player.collisionBounds.width / 2f, player.collisionBounds.y);
            }
        } else if (climbIn || climbOut) {
            climbAnimTime += dt;
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
        player.hide = player.freeze = true;

        // play zombie build animation right where player was standing
        buildingMech = true;
        zombieMechBuildAnimTime = 0f;

        particles.makeZombieBuildClouds(player.collisionBounds.x + player.collisionBounds.width / 2f, player.collisionBounds.y);

        // when done, spawn mech, reshow and reenable input for player
        Timeline.createSequence()
                .delay(assets.mechBuildAnimation.getAnimationDuration() + 0.25f)
                .push(Tween.call((type, source) -> {
                    buildingMech = false;
                    zombieMech = new ZombieMech(GameScreen.this,
                            // hack alert!
                            player.position.x, player.position.y - player.collisionBounds.height /2 + 50 );
                    physicsEntities.add(zombieMech);
                    player.hide = false;
                    player.freeze = false;
                }))
                .start(game.tween);
    }

    public void climbInMech() {
        pausePlayer();

        climbIn = true;
        climbAnimTime = 0f;

        Timeline.createSequence()
                .delay(assets.playerEnterMech.getAnimationDuration())
                .push(Tween.call((type, source) -> {
                    climbIn = false;
                    restorePlayer();
                    game.audio.fadeMusic(Audio.Musics.barkMusic);
                }))
                .start(game.tween);
    }

    private void pausePlayer() {
        player.hide = player.freeze = true;

        if (zombieMech != null) {
            zombieMech.freeze = true;
        }
    }

    private void restorePlayer() {
        player.hide = player.freeze = false;
        player.velocity.set(0, 0);
        player.state = GameEntity.State.standing;

        if (zombieMech != null) {
            zombieMech.freeze = false;
            zombieMech.velocity.set(0, 0);
            zombieMech.state = GameEntity.State.standing;
        }
    }

    // NO NO NO
//    public void climbOutMech() {
//        pausePlayer();
//
//        climbOut = true;
//        climbAnimTime = 0f;
//        Timeline.createSequence()
//                .delay(assets.playerLeaveMech.getAnimationDuration())
//                .push(Tween.call((type, source) -> {
//                    climbOut = false;
//                    restorePlayer();
//                    game.audio.fadeMusic(Audio.Musics.ritzMusic);
//                }))
//                .start(game.tween);
//    }

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

        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            buildZombieMech();
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

    public void addMarker(GameEntity entity) {
        GraveMarker marker = new GraveMarker(this);

        float x = entity.position.x;
        float y = entity.position.y + (entity.collisionBounds.height - marker.height)/2 + 1;
        marker.setPosition(x, y);
        randomShit.add(marker);
    }
}
