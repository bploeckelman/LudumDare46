package lando.systems.ld46.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld46.Game;

public class LaunchScreen extends BaseScreen {

    public LaunchScreen(Game game) {
        super(game);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (Gdx.input.justTouched()){
            game.setScreen(new GameScreen(game), assets.cubeShader, 3f);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.begin();
        batch.setProjectionMatrix(shaker.getCombinedMatrix());

        batch.draw(assets.launchImage, 0,0, shaker.getViewCamera().viewportWidth, shaker.getViewCamera().viewportHeight);

        batch.end();
    }
}
