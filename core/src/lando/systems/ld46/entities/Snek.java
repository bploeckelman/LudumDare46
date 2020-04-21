package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld46.Audio;
import lando.systems.ld46.screens.GameScreen;

public class Snek extends EnemyEntity {

    public Snek(GameScreen screen) {
        super(screen, screen.assets.snakeAnimation, 2);

        damage = 8;

        setSounds(Audio.Sounds.snek_hurt, Audio.Sounds.snek_death);

        // hack the planet!
        renderRotation = 90;
        collisionBounds.set(0, 0, 30, collisionBounds.width);
        setHealth(25f);
    }

    @Override
    public void render(SpriteBatch batch) {
        imageBounds.setPosition(position.x - imageBounds.width / 2f, position.y - collisionBounds.height / 2f);
        imageBounds.x -= 17;
        super.render(batch);
    }
}
