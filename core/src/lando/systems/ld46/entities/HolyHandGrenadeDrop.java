package lando.systems.ld46.entities;

import lando.systems.ld46.Audio;
import lando.systems.ld46.screens.GameScreen;

public class HolyHandGrenadeDrop extends DropEntity {

    public HolyHandGrenadeDrop(GameScreen screen) {
        super(screen, screen.assets.holyHandGrenadeDropAnimation, MoveEntityIds.Doctor|MoveEntityIds.Zombie);

        initEntity(0, 0, keyframe.getRegionWidth(), keyframe.getRegionHeight());

        // super powerful - cut the time
        pickupSound = Audio.Sounds.pickup_handgrenade;
        dropDuration = 5;
    }

    @Override
    public void applyDrop(MoveEntity mover) {
        // kill em all, let the engine sort it out
        for (EnemyEntity e : screen.enemies) {
            e.takeDamage(e.hitPoints);
        }
    }
}
