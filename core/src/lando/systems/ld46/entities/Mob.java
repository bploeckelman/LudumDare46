package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld46.screens.GameScreen;

public class Mob extends EnemyEntity {

    private Array<MobEntity> mobEntities;

    public Mob(GameScreen screen, float x, float y) {
        super(screen, screen.assets.whitePixel);

        int count = MathUtils.random(3, 7);

        mobEntities = new Array<>(count);

        while (count-- > 0) {
            MobEntity entity = new MobEntity(screen, x, y);
            mobEntities.add(entity);
            screen.physicsEntities.add(entity);
        }

        initEntity(x, y, 1, 1);
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        for (MobEntity entity : mobEntities) {
            entity.update(dt);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        for (MobEntity entity : mobEntities) {
            entity.render(batch);
        }

        // when there is a main guy, render this in front
        // super.render(batch);
    }

    @Override
    public void cleanup() {
        screen.physicsEntities.removeAll(mobEntities, false);
    }
}
