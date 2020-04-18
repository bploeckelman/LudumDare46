package lando.systems.ld46.entities;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import lando.systems.ld46.screens.GameScreen;

public class Player extends GameEntity {

    enum JumpState { none, jumping }

    private JumpState jumpState;

    private final float jumpVelocity = 3000f;
    private final float horizontalSpeed = 500f;
    private final float horizontalSpeedMinThreshold = 50f;
    private final float horizontalJoystickThreshold = 0.2f;

    public Player(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.playerAnimation);
        this.collisionBounds.set(x, y, keyframe.getRegionWidth(), keyframe.getRegionHeight());
        this.imageBounds.set(x, y, keyframe.getRegionWidth(), keyframe.getRegionHeight());
        this.position.set(x, y);
        this.jumpState = JumpState.none;
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        // Horizontal ----------------------------------------

        // Check for and apply horizontal movement
        boolean moveLeftPressed = Gdx.input.isKeyPressed(Input.Keys.A)
                || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean moveRightPressed = Gdx.input.isKeyPressed(Input.Keys.D)
                || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        if (moveLeftPressed) {
            velocity.add(-horizontalSpeed, 0);
            direction = Direction.left;
        } else if (moveRightPressed) {
            velocity.add(horizontalSpeed, 0);
            direction = Direction.right;
        }

        // Apply horizontal drag
        if (!grounded && !moveLeftPressed && !moveRightPressed) {
            velocity.x = 0f;
        } else {
            velocity.x *= 0.85f;
        }

        // Clamp minimum horizontal velocity to zero
        if (Math.abs(velocity.x) < horizontalSpeedMinThreshold) {
            velocity.x = 0f;
        }

        // Vertical ------------------------------------------

        boolean jumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.W);
        if (jumpPressed) {
            jump();
        }
    }

    private void jump() {
        jump(1f);
    }

    private void jump(float velocityMultiplier) {
        if (grounded) {
            velocity.y = jumpVelocity * velocityMultiplier;
            jumpState = JumpState.jumping;
            grounded = false;
        }
    }

    @Override
    public void changeDirection() {
        // noop so it doesn't flip rapidly when pushing against a wall.
    }

}