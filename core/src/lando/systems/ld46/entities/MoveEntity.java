package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    private Audio.Sounds punchSound = Audio.Sounds.none;
    private float punchDamage = 0f;

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

    public void setPunch(Animation<TextureRegion> punchAnimation, Audio.Sounds punchSound, float punchDamage) {
        this.punchAnimation = punchAnimation;
        this.punchSound = punchSound;
        this.punchDamage = punchDamage;
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if (velocity.y < 0) {
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
        if (jumpTime != -1) {
            jumpTime += dt;
            if (state == State.jumping && (jumpAnimation == null || jumpTime > jumpAnimation.getAnimationDuration())) {
                playSound(jumpSound);
                velocity.y = jumpVelocity;
                state = State.jump;
            } else if (jumpAnimation != null) {
                keyframe = jumpAnimation.getKeyFrame(jumpTime);
            }
        }
    }

    private void updatePunch(float dt) {
        if (punchTime != -1) {
            punchTime += dt;
            if (punchTime > punchAnimation.getAnimationDuration()) {
                punchTime = -1;
            } else {
                keyframe = punchAnimation.getKeyFrame(punchTime);
            }
        }
    }

    public void jump() {
        if (jumpTime == -1 && grounded) {
            jumpTime = 0;
            state = State.jumping;
        }
    }

    public void punch() {
        if (punchTime == -1 && state != State.jumping && punchAnimation != null) {
            playSound(punchSound);
            punchTime = 0;
        }
    }

    public void bleed() {
        screen.particles.makeBloodParticles(position.x, position.y);
    }
}
