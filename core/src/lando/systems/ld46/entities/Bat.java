package lando.systems.ld46.entities;

import lando.systems.ld46.screens.GameScreen;

public class Bat extends EnemyEntity {
    protected Bat(GameScreen screen) {
        super(screen, screen.assets.batAnimation, 2);

        hitPoints = 20;
    }
}
