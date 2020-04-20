package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.ld46.screens.GameScreen;

public class DropEntity extends GameEntity {

    protected float dropDuration = 8;

    private float dropTimer = 0;

    public DropEntity(GameScreen screen, Animation<TextureRegion> animation) {
        super(screen, animation);

        setHealth(0);
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        dropTimer -= dt;
        if (dropTimer <= 0) {
            removeFromScreen();
        }
    }

    public void useOn(MoveEntity mover) {
        // override to do shit
    }

    public void addToScreen(float x, float y) {
        setPosition(x, y);
        screen.drops.add(this);
        screen.physicsEntities.add(this);

        dropTimer = dropDuration;
    }

    public void removeFromScreen() {
        screen.drops.removeValue(this, true);
        screen.physicsEntities.removeValue(this, true);
    }

    @Override
    public Color getEffectColor() {
        if ((dropTimer * 2) < dropDuration) {
            if ((int)(dropTimer * 30) % 2 == 0 ) { return Color.BLACK; }
        }
        return super.getEffectColor();
    }
}
