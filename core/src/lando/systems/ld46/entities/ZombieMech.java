package lando.systems.ld46.entities;

import com.badlogic.gdx.math.Rectangle;
import lando.systems.ld46.Audio;
import lando.systems.ld46.screens.GameScreen;

public class ZombieMech extends MoveEntity {

    public float moveModifier = 0.5f;

    public ZombieMech(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.mechAnimation, screen.game.assets.mechMoveAnimation);

        setJump(screen.game.assets.mechJumpAnimation, Audio.Sounds.zombie_jump, 350f);
        setPunch(screen.game.assets.mechAttackAnimation, Audio.Sounds.zombie_punch, Audio.Sounds.zombie_punch_land, new int[]{2},100f);
        setFall(screen.game.assets.mechFallAnimation);

        initEntity(x, y, keyframe.getRegionWidth() * 2, keyframe.getRegionHeight() * 2);
    }
    @Override
    protected void initEntity(float x, float y, float width, float height) {
        imageBounds.set(x, y, width, height);
        float paddingX = (3f / 5f) * width;
        float paddingY = (1f / 5f) * height;
        collisionBounds.set(x + paddingX / 2f, y + paddingY / 2f, width - paddingX, height - paddingY);
        setPosition(x, y);
    }

    @Override
    public void move(Direction direction, float speed) {
        super.move(direction, speed * moveModifier);
    }

    @Override
    public boolean updateStateTimer() {
        return true;
    }

    Rectangle punchRect = new Rectangle(0, 0, 10, 10);
    @Override
    protected Rectangle getPunchRect() {
        float x = (direction == Direction.left) ? collisionBounds.x - 25 : collisionBounds.x + collisionBounds.width + 15;
        punchRect.setPosition(x, collisionBounds.y + collisionBounds.height - 40);
        return punchRect;
    }

    @Override
    protected Rectangle handleDamage() {
        Rectangle r = super.handleDamage();
        // check for punches against punchWalls in the level
        screen.level.punchWalls.forEach(wall -> {
            if (wall.bounds.contains(r)) {
                Direction punchDir = (wall.center.x < position.x) ? Direction.left : Direction.right;
                wall.punch(punchDir);
                playSound(punchHitSound);
                bleed(direction, r.x + r.width / 2, r.y + r.height / 2);
            }
        });
        return r;
    }

    public void explode() {
        screen.particles.makeExplodingZombieParticles(position.x, position.y);
    }

    @Override
    public void takeDamage(float damage) {
        hitPoints -= damage;
        if (hitPoints <= 0) {
            dead = true;
            explode();
        }
    }

    public void resetMech() {
        hitPoints = 100f;
        dead = false;
    }
}
