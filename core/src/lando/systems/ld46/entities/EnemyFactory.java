package lando.systems.ld46.entities;

import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld46.screens.GameScreen;

public class EnemyFactory {
    enum EnemyType {
        bat, snake, mob
    }

    public static void AddEnemy(GameScreen screen, float x, float y) {
        EnemyType[] types = EnemyType.values();
        AddEnemy(screen, types[MathUtils.random(types.length - 1)], x, y);
    }

    public static void AddEnemy(GameScreen screen, EnemyType type, float x, float y) {
        EnemyEntity enemy = null;
        switch (type) {
            case bat:
                enemy = new Bat(screen);
                break;
            case snake:
                enemy = new Snek(screen);
                break;
            case mob:
                enemy = new Mob(screen);
                break;
        }

        enemy = new Mob(screen);

        if (enemy == null) return;

        enemy.addToScreen(x, y);
    }
}
