package lando.systems.ld46.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import lando.systems.ld46.Audio;
import lando.systems.ld46.screens.GameScreen;

public class MoveEntity extends GameEntity {

    public static float JUMP_BONUS = .5f;

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
    private float jumpKeyHeldTimer = 0f;

    protected float invulnerabilityTimer = 0;

    // for when doc is in the zombie
    protected boolean ignore = false;

    // bitwise id
    public int id;

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

    // punch rect display
//    @Override
//    public void render(SpriteBatch batch) {
//        super.render(batch);
//        batch.setColor(Color.RED);
//        batch.draw(screen.assets.whitePixel, punchRect.x, punchRect.y, punchRect.width, punchRect.height);
//        batch.setColor(Color.WHITE);
//    }

    @Override
    public void update(float dt) {
        if (ignore) return;

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

        if (invulnerabilityTimer > 0) {
            invulnerabilityTimer -= dt;
        }

        // checks both sides
        updateDamage();
        updateDrops();
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

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)){
            jumpKeyHeldTimer += dt;
        }
        jumpTime += dt;
        if (jumpAnimation != null) {
            keyframe = jumpAnimation.getKeyFrame(jumpTime);
        }

        if (state == State.jumping && (jumpAnimation == null || jumpTime > jumpAnimation.getAnimationDuration())) {
            playSound(jumpSound);
            float bonusJump = (jumpKeyHeldTimer / jumpAnimation.getAnimationDuration()) * JUMP_BONUS;
            velocity.y = jumpVelocity * (1f + bonusJump);
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

        // now player can hit multiple targets in same frame
        // cannot take damage on frame they hit - probably will next frame

        boolean damageTaken = false;

        // go through every enemy checking for damage (giving or taking)
        for (EnemyEntity enemy : screen.enemies) {
            if (enemy.dead) {
                continue;
            }

            if (checkPunch && punchRect.overlaps(enemy.collisionBounds)) {
                damageEnemy(enemy, direction, punchDamage);
                // only damage one enemy
                // checkPunch = false;
            } else {
                // don't take damage after hitting an enemy

                // don't take damage for a smidge
                if (invulnerabilityTimer > 0) {
                    continue;
                }

                if (collisionBounds.overlaps(enemy.collisionBounds)) {
                    takeDamage(enemy.damage);
                    if (!dead) {
                        // bounce back if not dead
                        float distance = enemy.damage * 50; // modify for size
                        velocity.add((position.x > enemy.position.x) ? distance : -distance, MathUtils.random(10, 20));
                    }
                    damageTaken = true;
                }
            }
        }

        if (damageTaken) {
            invulnerabilityTimer = 2f;
        }
    }

    private void damageEnemy(EnemyEntity enemy, Direction direction, float damage) {
        playSound(punchHitSound);
        bleed(direction, punchRect.x + punchRect.width / 2, punchRect.y + punchRect.height / 2);
        enemy.takeDamage(damage);

        float distance = damage * 20;
        if (damage > 40 && MathUtils.random(0, 100) > 50) { // should be 80, but lower for more fun
            enemy.maxHorizontalVelocity = 100000;
            distance = 2500;
        }

        enemy.velocity.add(direction == Direction.right ? distance : -distance, MathUtils.random(20, 40));
    }

    public void updateDrops() {
        for (int i = screen.drops.size - 1; i >= 0; i--) {
            DropEntity drop = screen.drops.get(i);
            if (collisionBounds.overlaps(drop.collisionBounds)) {
                if (drop.useOn(this)) {
                    drop.removeFromScreen();
                }
            }
        }
    }

    public void jump() {
        if (jumpTime == -1 && grounded) {
            jumpTime = 0;
            jumpKeyHeldTimer = 0;
            state = State.jumping;
        }
    }

    public void punch() {
        if (punchTime == -1 && canPunch() && punchAnimation != null) {
            playSound(punchSwingSound);
            punchTime = 0;
        }
    }

    public void bleed(Direction direction, float x, float y) {
        screen.particles.makeBloodParticles(direction, x, y);
    }

    private boolean canPunch() {
        return !(state == State.jumping || state == State.falling);
    }

    @Override
    public boolean isInvulnerable() {
        return invulnerabilityTimer > 0;
    }

    @Override
    public Color getEffectColor() {
        if (isInvulnerable()) {
            if ((int)(invulnerabilityTimer * 20) % 2 == 0 ) { return Color.RED; }
        }
        return super.getEffectColor();
    }

    public void addHealth(float health) {
        hitPoints = Math.min(maxHealth, hitPoints + health);
    }
}
