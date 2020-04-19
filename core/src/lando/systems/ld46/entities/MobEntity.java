package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld46.Assets;
import lando.systems.ld46.screens.GameScreen;

public class MobEntity extends EnemyEntity {

    private static Animation<TextureRegion> getAnimation(Assets assets) {
        return MathUtils.randomBoolean() ? assets.mobPitchforkAnimation : assets.mobTorchAnimation;
    }

    public MobEntity(GameScreen screen, float x, float y) {
        super(screen, MobEntity.getAnimation(screen.assets));

        stateTime = MathUtils.random();
        direction = MathUtils.randomBoolean() ? Direction.left : Direction.right;

        initEntity(x, y, keyframe.getRegionWidth() * 2f, keyframe.getRegionHeight() * 2f);
    }
}
