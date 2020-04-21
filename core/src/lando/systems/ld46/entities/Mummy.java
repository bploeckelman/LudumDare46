package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld46.Audio;
import lando.systems.ld46.screens.GameScreen;

public class Mummy extends EnemyEntity {
    private float moveTimer = MathUtils.random(10f, 15f);
    private float turnTimer = MathUtils.random(1f, 3f);

    public Mummy(GameScreen screen) {
        super(screen, screen.assets.mummyAnimation, 1.5f);

        damage = 10f;

        setHealth(50f);
    }

    @Override
    public void render(SpriteBatch batch) {
        imageBounds.x -= 17;
        super.render(batch);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        moveTimer-=dt;
        turnTimer-=dt;

        if (turnTimer < 0) {
            if (Math.abs(screen.player.position.x - position.x) < 700f) {
                direction = (screen.player.position.x < position.x) ? Direction.left : Direction.right;
            } else {
                direction = MathUtils.randomBoolean() ? Direction.left : Direction.right;
            }
            turnTimer = MathUtils.random(1f, 3f);
        }
        if (moveTimer > 1f) {
            velocity.x = direction == Direction.left ? -50f : 50f;
        } else {
            velocity.x = direction == Direction.left ? -5f : 5f;
        }
        if (moveTimer < 0f) {
            moveTimer = MathUtils.random(10f, 15f);
        }
    }

}
