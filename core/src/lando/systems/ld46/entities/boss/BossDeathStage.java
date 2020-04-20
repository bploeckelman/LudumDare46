package lando.systems.ld46.entities.boss;

import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld46.screens.GameScreen;

public class BossDeathStage implements BossStage {

    Boss boss;
    GameScreen screen;

    float timer;
    float disappearTime = 5f;

    public BossDeathStage(Boss boss) {
        this.boss = boss;
        this.screen = boss.screen;
        boss.accum = screen.assets.bossHurtAnimation.getAnimationDuration() - .01f;
        boss.currentAnimation = screen.assets.bossHurtAnimation;
        boss.keyframe = boss.currentAnimation.getKeyFrame(boss.accum);
        timer = 0;
    }

    @Override
    public void update(float dt) {
        timer += dt;
        boss.accum = screen.assets.bossHurtAnimation.getAnimationDuration() - .01f;
        boss.currentAnimation = screen.assets.bossHurtAnimation;
        boss.position.z = MathUtils.clamp((disappearTime - timer)/disappearTime, 0, 1f);
    }

    @Override
    public boolean isComplete() {
        return timer > disappearTime + 1;
    }

    @Override
    public BossStage nextStage() {
        return null;
    }
}
