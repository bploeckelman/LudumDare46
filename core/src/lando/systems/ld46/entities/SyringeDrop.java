package lando.systems.ld46.entities;

import lando.systems.ld46.screens.GameScreen;

public class SyringeDrop extends DropEntity {

    public SyringeDrop(GameScreen screen) {
        super(screen, screen.assets.syringeDropAnimation, MoveEntityIds.Doctor);
        initEntity(0, 0, keyframe.getRegionWidth() * 1.5f, keyframe.getRegionHeight() * 1.5f);
    }

    @Override
    public void applyDrop(MoveEntity mover) {
        mover.addHealth(30);
    }
}
