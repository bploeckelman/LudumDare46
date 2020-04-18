package lando.systems.ld46.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld46.Game;
import lando.systems.ld46.entities.Player;
import lando.systems.ld46.world.Level;
import lando.systems.ld46.world.LevelDescriptor;

public class GameScreen extends BaseScreen {

    public Player player;
    public Level testLevel;

    public GameScreen(Game game) {
        super(game);

        this.testLevel = new Level(LevelDescriptor.test, this);
        this.player = new Player(this, 300, 300);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        {
            testLevel.render(worldCamera);
            player.render(batch);
        }
        batch.end();
    }

    @Override
    public void update(float dt) {
        player.update(dt);
        testLevel.render(worldCamera);
    }

}
