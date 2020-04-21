package lando.systems.ld46.entities;

import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld46.Audio;
import lando.systems.ld46.screens.GameScreen;

public class Bat extends EnemyEntity {

    private float flyTimer = 0;

    public Bat(GameScreen screen) {
        super(screen, screen.assets.batAnimation, 2);

        setSounds(Audio.Sounds.bat_hurt, Audio.Sounds.bat_death);

        collisionBounds.height = 30;
        setHealth(10);
        damage = 5;
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if (!dead) {
            // flutter
            velocity.add(0, MathUtils.random(600, 700) * dt);

            flyTimer += dt;
            if (flyTimer > 5) {
                velocity.set(MathUtils.random(-200f, 200f), MathUtils.random(200f, 400f));
                flyTimer = 0;
            }
        }
    }
}
