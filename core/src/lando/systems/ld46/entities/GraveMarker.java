package lando.systems.ld46.entities;

import lando.systems.ld46.screens.GameScreen;

public class GraveMarker extends GameEntity {

    public GraveMarker(GameScreen screen) {
        super(screen, screen.assets.grave);
        initEntity(0, 0, keyframe.getRegionWidth() * 2, keyframe.getRegionHeight() * 2);

        // no hitpoints
        setHealth(0);
    }
}
