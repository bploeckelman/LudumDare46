package lando.systems.ld46.entities;

import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld46.screens.GameScreen;

public class Bat extends EnemyEntity {

    private float flyTimer = 0;

    public Bat(GameScreen screen) {
        super(screen, screen.assets.batAnimation, 2);

        collisionBounds.height = 30;
        setHealth(20);
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if (!dead) {
            flyTimer += dt;
            if (flyTimer > 5) {
                velocity.set(MathUtils.random(-200f, 200f), MathUtils.random(400f, 600f));
                flyTimer = 0;
            }
        }
    }
}
