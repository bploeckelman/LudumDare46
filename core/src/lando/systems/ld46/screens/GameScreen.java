package lando.systems.ld46.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld46.Game;
import lando.systems.ld46.entities.Player;
import lando.systems.ld46.world.Level;
import lando.systems.ld46.world.LevelDescriptor;
import lando.systems.ld46.entities.ZombieMech;
import lando.systems.ld46.particles.Particles;

public class GameScreen extends BaseScreen {

    public Player player;
    public Level level;

    public ZombieMech zombieMech;

    public GameScreen(Game game) {
        super(game);

        this.level = new Level(LevelDescriptor.test, this);
        this.player = new Player(this, 300, 300);
        this.zombieMech = new ZombieMech(this, 400, 300);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(worldCamera.combined);
        {
            level.render(Level.LayerType.background, worldCamera);
            level.render(Level.LayerType.collision, worldCamera);
            batch.begin();
            {
                player.render(batch);
                zombieMech.render(batch);
                particles.draw(batch, Particles.Layer.foreground);
            }
            batch.end();
            level.render(Level.LayerType.foreground, worldCamera);
        }
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if (Gdx.input.justTouched()) {
            particles.addParticles(MathUtils.random(worldCamera.viewportWidth), MathUtils.random(worldCamera.viewportHeight));
        }
        particles.update(dt);
        level.update(dt);
        player.update(dt);
        zombieMech.update(dt);
    }

}
