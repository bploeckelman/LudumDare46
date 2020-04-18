package lando.systems.ld46.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld46.Game;
import lando.systems.ld46.entities.Player;
import lando.systems.ld46.world.Level;
import lando.systems.ld46.world.LevelDescriptor;
import lando.systems.ld46.entities.ZombieMech;

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
            }
            batch.end();
            level.render(Level.LayerType.foreground, worldCamera);
        }
    }

    @Override
    public void update(float dt) {
        level.update(dt);
        player.update(dt);
        zombieMech.update(dt);
    }

}
