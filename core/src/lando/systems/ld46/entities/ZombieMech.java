package lando.systems.ld46.entities;

import lando.systems.ld46.screens.GameScreen;

public class ZombieMech extends GameEntity {

    public float moveModifier = 0.5f;

    public ZombieMech(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.mechAnimation);
        this.collisionBounds.set(x, y, keyframe.getRegionWidth(), keyframe.getRegionHeight() * 2);
        this.imageBounds.set(x, y, keyframe.getRegionWidth(), keyframe.getRegionHeight() * 2);
        this.setPosition(x, y);
    }

    public void move(Direction direction, float speed) {
        super.move(direction, speed * moveModifier);
    }
}
