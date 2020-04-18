package lando.systems.ld46.entities;

import lando.systems.ld46.screens.GameScreen;

public class ZombieMech extends GameEntity {

    public ZombieMech(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.mechAnimation);
        this.collisionBounds.set(x, y, keyframe.getRegionWidth(), keyframe.getRegionHeight() * 2);
        this.imageBounds.set(x, y, keyframe.getRegionWidth(), keyframe.getRegionHeight() * 2);
        this.position.set(x, y);
    }
}
