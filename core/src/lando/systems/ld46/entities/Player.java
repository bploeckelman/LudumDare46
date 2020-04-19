package lando.systems.ld46.entities;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld46.Audio;
import lando.systems.ld46.screens.GameScreen;
import lando.systems.ld46.world.SpawnPlayer;

public class Player extends MoveEntity {

    private final float horizontalSpeed = 50f;

    private ZombieMech mech = null;

    public Player(GameScreen screen, SpawnPlayer spawn) {
        this(screen, spawn.pos.x, spawn.pos.y);
    }

    public Player(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.playerAnimation, screen.game.assets.playerMoveAnimation);

        setJump(screen.game.assets.playerJumpAnimation, Audio.Sounds.doc_jump, 450f);
        setPunch(screen.game.assets.playerAttackAnimation, Audio.Sounds.doc_punch, 10);
        setFall(screen.game.assets.playerFallAnimation);

        initEntity(x, y, keyframe.getRegionWidth() * 1.95f, keyframe.getRegionHeight() * 1.95f);
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        // this is the animation of starting to jump
        if (state != State.jumping) {
            // Check for and apply horizontal movement
            boolean moveLeftPressed = Gdx.input.isKeyPressed(Input.Keys.A)
                    || Gdx.input.isKeyPressed(Input.Keys.LEFT);
            boolean moveRightPressed = Gdx.input.isKeyPressed(Input.Keys.D)
                    || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

            if (moveLeftPressed) {
                move(Direction.left);
            } else if (moveRightPressed) {
                move(Direction.right);
            }
        }

        boolean jumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        if (jumpPressed) {
            jump();
        }

        boolean punchPressed = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        if (punchPressed) {
            punch();
            bleed();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            if (inMech()) {
                jumpOut();
            } else {
                ZombieMech mech = this.screen.zombieMech;
                if (collisionBounds.overlaps(mech.collisionBounds)) {
                    jumpIn(this.screen.zombieMech);
                }
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (inMech()) {
            return;
        }

        super.render(batch);
    }

    public boolean inMech() {
        return mech != null;
    }

    private void move(Direction direction) {
        if (inMech()) {
            mech.move(direction, horizontalSpeed);
            centerOn(mech);
        } else {
            move(direction, horizontalSpeed);
        }
    }

    @Override
    public void jump() {
        if (inMech()) {
            mech.jump();
        } else {
            super.jump();
        }
    }

    @Override
    public void punch() {
        if (inMech()) {
            mech.punch();
        } else {
            super.punch();
        }
    }

    @Override
    public void changeDirection() {
        // noop so it doesn't flip rapidly when pushing against a wall.
    }

    public void jumpIn(ZombieMech mech) {
        this.mech = mech;
         // add state and animation jumping up its ass - update rendering call
    }

    public void jumpOut() {
        if (inMech()) {
            setPosition(mech.position.x, mech.position.y + 20);
            mech = null;
        }
    }
}