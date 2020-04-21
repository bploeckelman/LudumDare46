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
        //TODO: this is janky on tutorials
        imageBounds.x -= 17;
        super.render(batch);
    }
}
