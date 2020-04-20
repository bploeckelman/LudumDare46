package lando.systems.ld46.entities;

import lando.systems.ld46.screens.GameScreen;

public class Snek extends EnemyEntity {

    public Snek(GameScreen screen) {
        super(screen, screen.assets.snakeAnimation, 2);

        damage = 8;

        collisionBounds.height = 30;
        setHealth(25f);
    }
}
