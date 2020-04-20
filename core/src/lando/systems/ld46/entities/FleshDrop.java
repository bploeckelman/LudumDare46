package lando.systems.ld46.entities;

import lando.systems.ld46.screens.GameScreen;

public class FleshDrop extends DropEntity {

    public FleshDrop(GameScreen screen) {
        super(screen, screen.assets.fleshDropAnimation);

        initEntity(0, 0, keyframe.getRegionWidth() * 1.5f, keyframe.getRegionHeight() * 1.5f);
    }
}
