package lando.systems.ld46.entities;

import lando.systems.ld46.Audio;
import lando.systems.ld46.screens.GameScreen;

public class ZombieMech extends MoveEntity {

    public float moveModifier = 0.5f;
    public float jumpVelocity = 250f;

    public ZombieMech(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.mechAnimation, screen.game.assets.mechAnimation);

        initEntity(x, y, keyframe.getRegionWidth() * 2, keyframe.getRegionHeight() * 2);
    }

    @Override
    public void move(Direction direction, float speed) {
        if (grounded) {
            super.move(direction, speed * moveModifier);
        }
    }

    public void jump(float velocityMultiplier) {
        if (grounded) {
            playSound(Audio.Sounds.zombie_jump);
            velocity.y = jumpVelocity * velocityMultiplier;
            grounded = false;
        }
    }
}
