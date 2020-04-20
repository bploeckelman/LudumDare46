package lando.systems.ld46.entities;

import lando.systems.ld46.Audio;
import lando.systems.ld46.screens.GameScreen;

public class FleshDrop extends DropEntity {

    public FleshDrop(GameScreen screen) {
        super(screen, screen.assets.fleshDropAnimation, MoveEntityIds.Zombie);

        pickupSound = Audio.Sounds.pickup_flesh;
        initEntity(0, 0, keyframe.getRegionWidth(), keyframe.getRegionHeight());
    }

    @Override
    public void applyDrop(MoveEntity mover) {
        mover.addHealth(70);
    }
}
