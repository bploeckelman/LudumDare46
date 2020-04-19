package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.ld46.screens.GameScreen;

public class EnemyEntity extends GameEntity {

    public float hitpoints = 10;
    public boolean dead = false;

    protected EnemyEntity(GameScreen screen, Animation<TextureRegion> animation) {
        super(screen, animation);
    }

    protected EnemyEntity(GameScreen screen, TextureRegion keyframe) {
        super(screen, keyframe);
    }

    public void takeDamage(float damage) {
        hitpoints -= damage;
        if (hitpoints <= 0) {
            dead = true;
        }
    }

    public void cleanup() {}
}
