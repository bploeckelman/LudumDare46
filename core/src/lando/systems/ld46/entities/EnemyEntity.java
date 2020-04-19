package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.ld46.screens.GameScreen;

public class EnemyEntity extends GameEntity {

    public float hitPoints = 10;
    public boolean dead = false;

    public float removeTime = 2f;
    public Feeler leftFeeler;
    public Feeler rightFeeler;

    protected EnemyEntity(GameScreen screen, Animation<TextureRegion> animation, float scale) {
        this(screen, animation);

        collisionBounds.set(0, 0, keyframe.getRegionWidth() * scale, keyframe.getRegionHeight() * scale);
        imageBounds.set(this.collisionBounds);
        leftFeeler = new Feeler(this, assets, -collisionBounds.width/2, 100);
        rightFeeler = new Feeler(this, assets, collisionBounds.width/2, 100);
    }

    protected EnemyEntity(GameScreen screen, Animation<TextureRegion> animation) {
        super(screen, animation);
    }

    public void addToScreen(float x, float y) {
        setPosition(x, y);
        screen.enemies.add(this);
        screen.physicsEntities.add(this);
    }

    public void removeFromScreen() {
        screen.enemies.removeValue(this, true);
        screen.physicsEntities.removeValue(this, true);
    }

    public void takeDamage(float damage) {
        hitPoints -= damage;
        if (hitPoints <= 0) {
            dead = true;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);
        leftFeeler.render(batch);
        rightFeeler.render(batch);
    }

    @Override
    public void update(float dt) {
        if (!dead) {
            super.update(dt);
            leftFeeler.update(dt);
            rightFeeler.update(dt);
        } else {
            removeTime -= dt;
            if (removeTime < 0) {
                removeFromScreen();
            }
        }
    }
}
