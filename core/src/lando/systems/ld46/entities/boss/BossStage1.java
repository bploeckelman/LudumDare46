package lando.systems.ld46.entities.boss;

import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld46.entities.Snek;
import lando.systems.ld46.screens.GameScreen;

public class BossStage1 implements BossStage {

    Boss boss;
    GameScreen screen;
    float timer;
    BossStage nextStage;
    float emergeTime;

    public BossStage1(Boss boss) {
        this.boss = boss;
        this.screen = boss.screen;
        timer = 0f;
        emergeTime = 5f;
        this.nextStage = new BossStage2(boss);
    }

    @Override
    public void update(float dt) {
        timer += dt;
        if (timer < 1f) {
            boss.position.z = 0;
            return;
        }
        boss.position.z = MathUtils.clamp((timer -1) / emergeTime, 0f, 1f);
    }

    @Override
    public boolean isComplete() {
        return timer >= emergeTime + 1f;
    }

    @Override
    public BossStage nextStage() {
        return nextStage;
    }
}
