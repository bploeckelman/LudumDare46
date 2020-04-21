package lando.systems.ld46.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld46.Assets;

public class MobEntity extends EnemyEntity {

    private static Animation<TextureRegion> getAnimation(Assets assets) {
        return MathUtils.randomBoolean() ? assets.mobPitchforkAnimation : assets.mobTorchAnimation;
    }

    private Mob boss;
    private float nextMoveTime;
    private float toX;
    private boolean doneMoving = false;
    private boolean initialPlacement = true;

    public MobEntity(Mob boss) {
        super(boss.screen, MobEntity.getAnimation(boss.screen.assets));

        this.boss = boss;

        stateTime = MathUtils.random();
        direction = MathUtils.randomBoolean() ? Direction.left : Direction.right;

        nextMoveTime = MathUtils.random(2f, 5f);

        setHealth(40);
        damage = 15;

        initEntity(0, 0, keyframe.getRegionWidth() * 2f, keyframe.getRegionHeight() * 2f);
    }

    private void setBehavior() {
        nextMoveTime = MathUtils.random(2f, 5f);
        toX = boss.position.x + MathUtils.random(-boss.influenceDistance, boss.influenceDistance);
        doneMoving = false;
    }

    @Override
    public void setGrounded(boolean groundValue) {
        if (grounded != groundValue) {
            super.setGrounded(groundValue);

            if (grounded) {
                setBehavior();
            }
        }
    }

    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        // stop the little bitches from moving
        doneMoving = true;
    }

    @Override
    public void update(float dt) {
        if (initialPlacement) {
            initialPlacement = false;
            return;
        }

        super.update(dt);

        if (boss.dead) {
            removeFromScreen();
        }

        if (!grounded || dead) return;

        if (!doneMoving) {
            if (toX > position.x + 5) {
                if (screen.physicsSystem.isPositionAboveGround(position.x + 20, collisionBounds.y + 30, 50)) {
                    velocity.x += 15;
                } else {
                    toX = position.x;
                    doneMoving = true;
                }
            } else if (toX < position.x - 5) {
                if (screen.physicsSystem.isPositionAboveGround(position.x - 20, collisionBounds.y + 30, 90)) {
                    velocity.x -= 15;
                } else {
                    toX = position.x;
                    doneMoving = true;
                }
            } else {
                toX = position.x;
                doneMoving = true;
            }
        } else {
            nextMoveTime -= dt;
            if (nextMoveTime < 0) {
                setBehavior();
            }
        }
    }
}
