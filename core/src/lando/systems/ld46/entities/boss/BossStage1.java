package lando.systems.ld46.entities.boss;

import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld46.entities.Snek;
import lando.systems.ld46.screens.GameScreen;

public class BossStage1 implements BossStage {

    Boss boss;
    GameScreen screen;
    float snekTimer;

    public BossStage1(Boss boss, GameScreen screen) {
        this.boss = boss;
        this.screen = screen;
        snekTimer = 10f;
    }

    @Override
    public void update(float dt) {
        // Some attacks or something here too

        snekTimer -= dt;
        if (snekTimer < 0) {
            snekTimer += 15;
            for (int i = 0 ; i < 4; i++) {
                Snek snek = new Snek(screen);
                // TODO: need to figure out what this is going to be in the final arena
                float x = MathUtils.random(60f, 400f);
                snek.addToScreen(x, 500);
                screen.particles.makeSpawnClouds(x, 500);
            }
        }
    }
}
