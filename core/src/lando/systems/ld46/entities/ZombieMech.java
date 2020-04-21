package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import lando.systems.ld46.Audio;
import lando.systems.ld46.screens.GameScreen;
import lando.systems.ld46.ui.GuideArrow;

public class ZombieMech extends MoveEntity {

    public static final float SCALE = 2f;

    public float moveModifier = 0.5f;

    private Audio.Sounds punchWallSound = Audio.Sounds.zombie_punch_wall;
    public GuideArrow mechIndicator;

    public ZombieMech(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.mechAnimation, screen.game.assets.mechMoveAnimation);

        setJump(screen.game.assets.mechJumpAnimation, Audio.Sounds.zombie_jump, 350f);
        setPunch(screen.game.assets.mechAttackAnimation, Audio.Sounds.zombie_punch, Audio.Sounds.zombie_punch_land, new int[]{2},100f);
        setFall(screen.game.assets.mechFallAnimation);

        setSounds(Audio.Sounds.zombie_hurt, Audio.Sounds.zombie_death);
        mechIndicator = new GuideArrow(screen, x, y, screen.assets.zombiePin);

        initEntity(x, y, keyframe.getRegionWidth() * SCALE, keyframe.getRegionHeight() * SCALE);

        id = MoveEntityIds.Zombie;

        setHealth(150);
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
        // allows animation at this point when dead
        return true;
    }

    @Override
    protected void updatePunchRect(Rectangle punchRect) {
        float x = (direction == Direction.left) ? collisionBounds.x - 35 : collisionBounds.x + collisionBounds.width + 25;
        punchRect.set(x, collisionBounds.y + collisionBounds.height - 55, 40, 40);
    }

    @Override
    protected void updatePunch(float dt) {
        super.updatePunch(dt);

        // check walls
        if (checkPunch) {
            // check for punches against punchWalls in the level
            screen.level.punchWalls.forEach(wall -> {
                if (wall.bounds.overlaps(punchRect)) {
                    Direction punchDir = (wall.center.x < position.x) ? Direction.left : Direction.right;
                    wall.punch(punchDir);
                    playSound(punchWallSound);
                    bleed(direction, punchRect.x + punchRect.width / 2, punchRect.y + punchRect.height / 2);
                }
            });
        }
    }

    public void explode() {
        screen.game.audio.fadeMusic(Audio.Musics.ritzMusic);
        screen.particles.makeExplodingZombieParticles(position.x, position.y);
        screen.physicsEntities.removeValue(this, true);
        screen.bodyBag.explodeParts(collisionBounds.x + collisionBounds.width / 2f, collisionBounds.y + collisionBounds.height / 2f);
    }

    @Override
    public void takeDamage(float damage) {
        if (!dead) {
            super.takeDamage(damage);
            if (dead) {
                explode();
            }
        }
    }
}
