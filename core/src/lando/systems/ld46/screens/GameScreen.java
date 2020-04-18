package lando.systems.ld46.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld46.Game;
import lando.systems.ld46.entities.Player;

public class GameScreen extends BaseScreen {

    public Player player;

    public GameScreen(Game game) {
        super(game);

        player = new Player(this, 300, 300);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.begin();

        player.render(batch);

        batch.end();
    }

    @Override
    public void update(float dt) {
        player.update(dt);
    }
}
