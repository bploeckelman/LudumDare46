package lando.systems.ld46.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld46.Config;
import lando.systems.ld46.Game;

public class TitleScreen extends BaseScreen {

    Vector2 placeholder;
    Vector2 vel;
    int size = 200;

    public TitleScreen(Game game){
        super(game);
        placeholder = new Vector2(MathUtils.random(Config.windowWidth - size), MathUtils.random(Config.windowHeight - size));
        vel = new Vector2(MathUtils.random(-1f, 1), MathUtils.random(-1f, 1f)).nor().scl(100);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        placeholder.add(vel.x * dt, vel.y * dt);
        if (placeholder.x < 0) {
            placeholder.x = 0;
            vel.x *= -1;
        }
        if (placeholder.y < 0) {
            placeholder.y = 0;
            vel.y *= -1;
        }
        if (placeholder.x > Config.windowWidth - size) {
            placeholder.x = Config.windowWidth - size;
            vel.x *= -1;
        }
        if (placeholder.y > Config.windowHeight - size) {
            placeholder.y = Config.windowHeight - size;
            vel.y *= -1;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.begin();
        batch.setProjectionMatrix(shaker.getCombinedMatrix());

        batch.draw(assets.debugTexture, placeholder.x, placeholder.y, size, size);

        batch.end();
    }
}
