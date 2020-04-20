package lando.systems.ld46.entities;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld46.Audio;
import lando.systems.ld46.screens.GameScreen;
import lando.systems.ld46.world.SpawnPlayer;

public class Player extends MoveEntity {

    private final float horizontalSpeed = 50f;

    private ZombieMech mech = null;
    private float inMechTimer = 0f;

    // for when building the mech suit, ignore input, hide sprite while playing build animation
    public boolean freeze = false;
    public boolean hide = false;


    public Player(GameScreen screen, SpawnPlayer spawn) {
        this(screen, spawn.pos.x, spawn.pos.y);
    }

    public Player(GameScreen screen, float x, float y) {
        super(screen, screen.game.assets.playerAnimation, screen.game.assets.playerMoveAnimation);

        setJump(screen.game.assets.playerJumpAnimation, Audio.Sounds.doc_jump, 450f);
        setPunch(screen.game.assets.playerAttackAnimation, Audio.Sounds.doc_punch, Audio.Sounds.doc_punch_land, new int[]{2, 3},10);
        setFall(screen.game.assets.playerFallAnimation);

        setSounds(Audio.Sounds.doc_hurt, Audio.Sounds.doc_death);

        initEntity(x, y, keyframe.getRegionWidth() * 1.95f, keyframe.getRegionHeight() * 1.95f);
    }

    @Override
    protected void initEntity(float x, float y, float width, float height) {
        imageBounds.set(x, y, width, height);
        float paddingX = (1f / 2f) * width;
        collisionBounds.set(x + paddingX / 2f, y, width - paddingX, height);
        showHeart = true;
        setPosition(x, y);
    }

    @Override
    public void update(float dt) {
        if (freeze) return;
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
        inMechTimer += dt;
        if (inMech() && !mech.dead && inMechTimer > .5f) {
            mech.takeDamage(5f);
            screen.particles.makeBloodParticles(mech.position.x, mech.position.y);
            inMechTimer = 0;
        }

        boolean jumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        if (jumpPressed) {
            jump();
        }

        boolean punchPressed = Gdx.input.justTouched();
        if (punchPressed) {
            punch();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            if (inMech()) {
                jumpOut();
                screen.game.audio.fadeMusic(Audio.Musics.ritzMusic);
            } else {
                ZombieMech mech = this.screen.zombieMech;
                if (mech != null && collisionBounds.overlaps(mech.collisionBounds)) {
                    mech.resetMech();
                    jumpIn(this.screen.zombieMech);
                    screen.game.audio.fadeMusic(Audio.Musics.barkMusic);
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            setPosition(position.x, 0);
        }

        if (inMech()){
            centerOn(mech);
        }

        catchHell();
    }

    private void catchHell() {
        if (position.y < -250) {
            Array<Rectangle> tiles = new Array<>();
            screen.level.getTiles(position.x, position.y, position.x, 10000, tiles);
            Rectangle r;
            float y = 0;
            for (int i = 0; i < tiles.size; i++) {
                r = tiles.get(i);
                if (r.y == y) {
                    y += r.height;
                } else {
                    if (y == 0) {
                        setPosition(screen.level.playerSpawn.pos.x, screen.level.playerSpawn.pos.y);
                    } else {
                        setPosition(position.x, y + collisionBounds.height / 2);
                        velocity.set(0, 400);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (hide) return;
        if (inMech()) return;

        super.render(batch);
    }

    public boolean inMech() {
        return mech != null;
    }

    private void move(Direction direction) {
        if (inMech()) {
            mech.move(direction, horizontalSpeed);
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
        mech.showHeart = true;
        showHeart = false;
         // add state and animation jumping up its ass - update rendering call
    }

    public void jumpOut() {
        if (inMech()) {
            setPosition(mech.position.x, mech.position.y + 20);
            mech.showHeart = false;
            showHeart = true;
            mech = null;
        }
    }

    @Override
    protected void updatePunchRect(Rectangle punchRect) {
        float x = (direction == Direction.left) ? collisionBounds.x - 25 : collisionBounds.x + collisionBounds.width + 15;
        punchRect.set(x, collisionBounds.y + collisionBounds.height - 25, 20, 20);
    }
}