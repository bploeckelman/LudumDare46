package lando.systems.ld46.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import lando.systems.ld46.Audio;
import lando.systems.ld46.Game;
import lando.systems.ld46.entities.Player;

import com.badlogic.gdx.math.Vector3;
import lando.systems.ld46.Config;
import lando.systems.ld46.Game;
import lando.systems.ld46.entities.Player;
import lando.systems.ld46.physics.PhysicsSystem;
import lando.systems.ld46.ui.typinglabel.TypingLabel;
import lando.systems.ld46.world.Level;
import lando.systems.ld46.world.LevelDescriptor;

import lando.systems.ld46.entities.ZombieMech;
import lando.systems.ld46.particles.Particles;
import lando.systems.ld46.world.Level;
import lando.systems.ld46.world.LevelDescriptor;

public class GameScreen extends BaseScreen {

    public Player player;
    public Level level;

    private Vector3 touchPos;
    private TypingLabel textLabel;
    public ZombieMech zombieMech;
    PhysicsSystem physicsSystem;

    public GameScreen(Game game) {
        super(game);
        this.physicsSystem = new PhysicsSystem(this);
        this.level = new Level(LevelDescriptor.test, this);
        this.player = new Player(this, level.playerSpawn);
        this.zombieMech = new ZombieMech(this, 400, 300);
        touchPos = new Vector3();
        textLabel = new TypingLabel(game.assets.riseFont16, "{JUMP=.2}{RAINBOW}hamster must die{ENDRAINBOW}{ENDJUMP}", 200f, 50f);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(worldCamera.combined);
        {
            level.render(Level.LayerType.background, worldCamera);
            level.render(Level.LayerType.collision, worldCamera);
            batch.begin();
            {
                level.renderObjectsDebug(batch);

                player.render(batch);
                zombieMech.render(batch);
                particles.draw(batch, Particles.Layer.foreground);
                textLabel.render(batch);
            }
            batch.end();
            level.render(Level.LayerType.foreground, worldCamera);

            if (Config.debug) {
                batch.begin();
                level.renderDebug(batch);
                batch.end();
            }
        }
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if (Gdx.app.getType() == Application.ApplicationType.Desktop && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        worldCamera.unproject(touchPos);

        if (Gdx.input.justTouched()) {
            particles.makePhysicsParticles(touchPos.x, touchPos.y);
            game.audio.playSound(Audio.Sounds.sample_sound, true);
        }
        particles.update(dt);
        physicsSystem.update(dt);
        level.update(dt);
        player.update(dt);
        zombieMech.update(dt);
        textLabel.update(dt);
    }

}
