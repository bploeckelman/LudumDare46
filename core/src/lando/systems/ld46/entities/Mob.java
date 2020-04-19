package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld46.screens.GameScreen;

public class Mob extends EnemyEntity {

    private Array<MobEntity> mobEntities;

    public float maxDistance;

    public Mob(GameScreen screen) {
        super(screen, screen.assets.playerAnimation, 0);

        int count = MathUtils.random(3, 7);

        mobEntities = new Array<>(count);

        float totalWidth = 0;
        while (count-- > 0) {
            MobEntity entity = new MobEntity(this);
            totalWidth += entity.collisionBounds.width;
            mobEntities.add(entity);
        }

        maxDistance = totalWidth/2;
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        for (MobEntity entity : mobEntities) {
            entity.setPosition(x, y);
        }

    }

    @Override
    public void addToScreen(float x, float y) {
        super.addToScreen(x, y);
        for (MobEntity entity : mobEntities) {
            entity.addToScreen(x, y);
        }
    }

    @Override
    public void removeFromScreen() {
        super.removeFromScreen();
        for (MobEntity entity : mobEntities) {
            entity.removeFromScreen();
        }
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
}
