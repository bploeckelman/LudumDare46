package lando.systems.ld46.entities;

import lando.systems.ld46.screens.GameScreen;

public class HolyHandGrenadeDrop extends DropEntity {

    public HolyHandGrenadeDrop(GameScreen screen) {
        super(screen, screen.assets.holyHandGrenadeDropAnimation, MoveEntityIds.Doctor|MoveEntityIds.Zombie);

        initEntity(0, 0, keyframe.getRegionWidth() * 2.5f, keyframe.getRegionHeight() * 2.5f);
    }

    @Override
    public void applyDrop(MoveEntity mover) {
        // kill em all, let the engine sort it out
        for (EnemyEntity e : screen.enemies) {
            e.takeDamage(e.hitPoints);
        }
    }
}
