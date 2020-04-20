package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import lando.systems.ld46.Audio;
import lando.systems.ld46.screens.GameScreen;

public class EnemyEntity extends GameEntity {

    public float removeTime = 2f;
    public Feeler leftFeeler;
    public Feeler rightFeeler;

    public float damage = 10f;

    protected Audio.Sounds deathSound = Audio.Sounds.none;
    protected Audio.Sounds attackSound = Audio.Sounds.none;

    protected EnemyEntity(GameScreen screen, Animation<TextureRegion> animation, float scale) {
        super(screen, animation);

        collisionBounds.set(0, 0, keyframe.getRegionWidth() * scale, keyframe.getRegionHeight() * scale);
        imageBounds.set(this.collisionBounds);

        leftFeeler = new Feeler(this, assets, -collisionBounds.width/2, 100);
        rightFeeler = new Feeler(this, assets, collisionBounds.width/2, 100);

        maxHorizontalVelocity = 1200;
    }

    protected EnemyEntity(GameScreen screen, Animation<TextureRegion> animation) {
        this(screen, animation, 1f);
    }

    public void addToScreen(float x, float y) {
        setPosition(x, y);
        screen.enemies.add(this);
        screen.physicsEntities.add(this);
    }

    public void removeFromScreen() {
        screen.enemies.removeValue(this, true);
        screen.physicsEntities.removeValue(this, true);
        spawnDrop();
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);
        leftFeeler.render(batch);
        rightFeeler.render(batch);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        leftFeeler.update(dt);
        rightFeeler.update(dt);

        if (dead) {
            removeTime -= dt;
            if (removeTime < 0) {
                removeFromScreen();
            }
        }
    }

    @Override
    public Color getEffectColor() {
        if (dead) {
            if ((int)(removeTime * 30) % 2 == 0 ) { return Color.BLACK; }
        }
        return super.getEffectColor();
    }

    private void spawnDrop() {
        // add more drops?
        DropEntity drop;

        float dropValue = MathUtils.random(0, 110);
        if (dropValue < 70) {
            drop = new FleshDrop(screen);
        } else if (dropValue < 100) {
            drop = new SyringeDrop(screen);
        } else {
            drop = new HolyHandGrenadeDrop(screen);
        }
        drop.addToScreen(position.x, position.y);
        drop.velocity.set(0, 20);
    }
}
