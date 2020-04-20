package lando.systems.ld46.entities.boss;

import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld46.entities.Bat;
import lando.systems.ld46.entities.EnemyEntity;
import lando.systems.ld46.entities.GameEntity;
import lando.systems.ld46.entities.Snek;
import lando.systems.ld46.screens.GameScreen;

public class BossStage4 implements BossStage {

    Boss boss;
    GameScreen screen;
    float snekTimer;
    float punchTimer;

    public BossStage4(Boss boss) {
        this.boss = boss;
        this.screen = boss.screen;
        snekTimer = 5f;
        punchTimer = 1f;
    }

    @Override
    public void update(float dt) {
        snekTimer -= dt;
        if (snekTimer < 0) {
            snekTimer += 10;
            for (int i = 0 ; i < 3; i++) {
                if (screen.enemies.size > 7) break;
                EnemyEntity entity = new Snek(screen);
                if (MathUtils.randomBoolean()) entity = new Bat(screen);
                float x = MathUtils.random(60f, 400);
                if (MathUtils.randomBoolean()) x += 800;
                entity.addToScreen(x, 700);
                screen.particles.makeSpawnClouds(x, 700);
            }
        }
        boss.flashRed = false;
        // Punch
        if (boss.currentAnimation == screen.assets.bossWalkAnimation) {
            punchTimer -= dt;
            if (punchTimer < 0) {
                punchTimer += MathUtils.random(3f, 7f);
                boss.currentAnimation = screen.assets.bossPunchAnimation;
                boss.accum = 0;
                boss.lastPunchIndex = -1;
                if (boss.position.x < screen.player.position.x) boss.direction = GameEntity.Direction.right;
                else boss.direction = GameEntity.Direction.left;
            } else if (punchTimer < 1f && punchTimer > .3f){
                if (punchTimer % .2f < .1f) boss.flashRed = true;
            }
        }
        else {
//            punchTimer = MathUtils.random(3f, 7f);;
        }
    }

    @Override
    public boolean isComplete() {

        return boss.hits > 4 && boss.currentAnimation == screen.assets.bossWalkAnimation;
    }

    @Override
    public BossStage nextStage() {
        return new BossDeathStage(boss);
    }
}
