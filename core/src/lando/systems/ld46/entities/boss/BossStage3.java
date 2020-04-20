package lando.systems.ld46.entities.boss;

import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld46.screens.GameScreen;

public class BossStage3 implements BossStage {

    Boss boss;
    GameScreen screen;
    float timer;
    BossStage nextStage;
    float emergeTime;
    float dir = -1;

    public BossStage3(Boss boss) {
        this.boss = boss;
        this.screen = boss.screen;
        emergeTime = 5f;
        timer = emergeTime-.01f;
        this.nextStage = new BossStage4(boss);
    }

    @Override
    public void update(float dt) {
        timer += (dir * dt);
        if (timer <= 0){
          dir = 1;
        }
        if (timer < 1f) {
            boss.position.z = 0;
        } else {
            boss.position.z = MathUtils.clamp((timer - 1) / emergeTime, 0f, 1f);
        }
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
