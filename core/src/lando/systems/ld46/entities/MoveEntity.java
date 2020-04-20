package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import lando.systems.ld46.Audio;
import lando.systems.ld46.screens.GameScreen;

public class MoveEntity extends GameEntity {

    private State lastState;

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> moveAnimation;

    private float fallTime = 0;
    private Animation<TextureRegion> fallAnimation;

    private float jumpTime = -1;
    private Animation<TextureRegion> jumpAnimation;
    private Audio.Sounds jumpSound = Audio.Sounds.none;
    private float jumpVelocity = 0f;

    private float punchTime = -1;
    private Animation<TextureRegion> punchAnimation;
    protected Audio.Sounds punchSwingSound = Audio.Sounds.none;
    protected Audio.Sounds punchHitSound = Audio.Sounds.none;
    protected Rectangle punchRect = new Rectangle();
    private int punchFrameIndex[];
    private float punchDamage = 0f;

    private float invlunerabilityTimer = 0;

    protected MoveEntity(GameScreen screen, Animation<TextureRegion> idle, Animation<TextureRegion> move) {
        super(screen, idle);

        idleAnimation = idle;
        moveAnimation = move;

        lastState = state;
    }

    public void setJump(Animation<TextureRegion> jumpAnimation, Audio.Sounds jumpSound, float jumpVelocity) {
        this.jumpAnimation = jumpAnimation;
        this.jumpSound = jumpSound;
        this.jumpVelocity = jumpVelocity;
    }

    public void setFall(Animation<TextureRegion> fallAnimation) {
        this.fallAnimation = fallAnimation;
    }

    public void setPunch(Animation<TextureRegion> punchAnimation, Audio.Sounds punchSwingSound, Audio.Sounds punchHitSound, int[] punchFrameIndex, float punchDamage) {
        this.punchAnimation = punchAnimation;
        this.punchSwingSound = punchSwingSound;
        this.punchHitSound = punchHitSound;
        this.punchFrameIndex = punchFrameIndex;
        this.punchDamage = punchDamage;
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if (velocity.y < -50) {
            state = State.falling;
            jumpTime = -1;
        }

        if (lastState != state) {
            if (state == State.standing) {
                setAnimation(idleAnimation);
            } else if (state == State.walking) {
                setAnimation(moveAnimation);
            }

            fallTime = 0;
            lastState = state;
        }

        updateFall(dt);
        updateJump(dt);
        updatePunch(dt);

        if (invlunerabilityTimer > 0) {
            invlunerabilityTimer -= dt;
        }

        // checks both sides
        updateDamage();
    }

    private void updateFall(float dt) {
        if (state == State.falling && fallAnimation != null) {
            fallTime += dt;
            keyframe = fallAnimation.getKeyFrame(fallTime);
        } else {
            fallTime = 0;
        }
    }

    private void updateJump(float dt) {
        if (jumpTime == -1) return;

        jumpTime += dt;
        if (jumpAnimation != null) {
            keyframe = jumpAnimation.getKeyFrame(jumpTime);
        }

        if (state == State.jumping && (jumpAnimation == null || jumpTime > jumpAnimation.getAnimationDuration())) {
            playSound(jumpSound);
            velocity.y = jumpVelocity;
            state = State.jump;
        }
    }

    private int lastPunchIndex = -1;
    protected boolean checkPunch = false;

    protected void updatePunch(float dt) {
        checkPunch = false;
        if (punchTime == -1) return;

        punchTime += dt;
        if (punchTime > punchAnimation.getAnimationDuration()) {
            punchTime = -1;
            lastPunchIndex = -1;
        } else {
            keyframe = punchAnimation.getKeyFrame(punchTime);
            if (isPunchFrame(punchTime)) {
                checkPunch = true;
                updatePunchRect(punchRect);
            }
        }
    }

    private boolean isPunchFrame(float time) {
        int punchIndex = punchAnimation.getKeyFrameIndex(time);
        for (int index : punchFrameIndex) {
            if (index == punchIndex && index != lastPunchIndex) {
                lastPunchIndex = index;
                return true;
            }
        }
        return false;
    }

    // override to find punch location
    protected void updatePunchRect(Rectangle punchRect) { }

    protected void updateDamage() {

        boolean damageTaken = false;

        // go through every enemy checking for damage (giving or taking)
        for (EnemyEntity enemy : screen.enemies) {
            if (enemy.dead) {
                continue;
            }

            if (checkPunch && punchRect.overlaps(enemy.collisionBounds)) {
                damageEnemy(enemy, direction, punchDamage);
                // only damage one enemy
                checkPunch = false;
            }

            // don't take damage for a smidge
            if (invlunerabilityTimer > 0) { continue; }

            if (collisionBounds.overlaps(enemy.collisionBounds)) {
                // assign damage to enemies
                // bounce back
                takeDamage(10);
                damageTaken = true;
            }
        }

        if (damageTaken) {
            invlunerabilityTimer = 2f;
        }
    }

    private void damageEnemy(EnemyEntity enemy, Direction direction, float damage) {
        playSound(punchHitSound);
        bleed(direction, punchRect.x + punchRect.width / 2, punchRect.y + punchRect.height / 2);
        enemy.takeDamage(punchDamage);
    }

    public void jump() {
        if (jumpTime == -1 && grounded) {
            jumpTime = 0;
            state = State.jumping;
        }
    }

    public void punch() {
        if (punchTime == -1 && canPunch() && punchAnimation != null) {
            playSound(punchSwingSound);
            punchTime = 0;
        }
    }

    public void bleed(float x, float y) {
        screen.particles.makeBloodParticles(x, y);
    }

    public void bleed(Direction direction, float x, float y) {
        screen.particles.makeBloodParticles(direction, x, y);
    }

    private boolean canPunch() {
        return !(state == State.jumping || state == State.falling);
    }

    public boolean isInvulnerable() {
        return invlunerabilityTimer > 0;
    }
}
