package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld46.Assets;

public class MobEntity extends EnemyEntity {

    private static Animation<TextureRegion> getAnimation(Assets assets) {
        return MathUtils.randomBoolean() ? assets.mobPitchforkAnimation : assets.mobTorchAnimation;
    }

    private float nextMoveTime;
    private Mob boss;
    private float fromX, toX;
    private float lerpTime = 0;

    public MobEntity(Mob boss) {
        super(boss.screen, MobEntity.getAnimation(boss.screen.assets));

        this.boss = boss;

        stateTime = MathUtils.random();
        direction = MathUtils.randomBoolean() ? Direction.left : Direction.right;

        nextMoveTime = MathUtils.random(2f, 5f);

        // prevent first update until physics engine has had a pass
        grounded = false;

        initEntity(0, 0, keyframe.getRegionWidth() * 2f, keyframe.getRegionHeight() * 2f);
    }

    private void setBehavior() {
        nextMoveTime = MathUtils.random(2f, 5f);

        direction = (position.x < boss.position.x) ? Direction.right : Direction.left;

        float distX = (direction == Direction.right) ? boss.maxDistance : -boss.maxDistance;

        // vector math may my brain shrivel, so I reverted to lerps - help me obi wan
        fromX = position.x;
        toX = position.x + (distX * MathUtils.random(0.2f, 0.5f));
        lerpTime = 0;
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
    public void update(float dt) {
        super.update(dt);

        if (!isGrounded()) return;

        if (position.x == toX) {
            nextMoveTime -= dt;
            if (nextMoveTime < 0) {
                setBehavior();
            }
        } else {
            lerpTime += dt;
            float pos = (lerpTime < 1) ? MathUtils.lerp(fromX, toX, lerpTime) : toX;
            setPosition(pos, position.y);
        }
    }
}
