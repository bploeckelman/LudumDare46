package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld46.Assets;
import lando.systems.ld46.Audio;
import lando.systems.ld46.Config;
import lando.systems.ld46.physics.PhysicsComponent;
import lando.systems.ld46.screens.GameScreen;
import lando.systems.ld46.ui.HealthMeter;

public class GameEntity implements PhysicsComponent {

    public enum Direction {
        right, left;
        public static Direction random() {
            return MathUtils.randomBoolean() ? right : left;
        }
    }
    public enum State { standing, walking, jumping, jump, falling }

    protected Assets assets;

    public GameScreen screen;
    protected TextureRegion keyframe;
    Animation<TextureRegion> animation;

    public State state = State.standing;
    public Direction direction = Direction.right;
    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();
    public float bounceScale = 0.8f;
    public Vector2 acceleration = new Vector2();
    public Rectangle imageBounds = new Rectangle();
    public Rectangle collisionBounds = new Rectangle();
    public Circle collisionCircle = new Circle();

    public boolean grounded;

    protected float stateTime;
    protected float maxHorizontalVelocity = 200f;
    private float maxVerticalVelocity = 1200f;
    private Array<Rectangle> tiles = new Array<>();

    public float maxHealth = 100f;
    public float hitPoints = 100f;
    public boolean dead = false;
    public HealthMeter healthMeter;
    public boolean showHeart = false;

    protected float renderRotation = 0;

    protected Audio.Sounds hurtSound = Audio.Sounds.none;
    protected Audio.Sounds deathSound = Audio.Sounds.none;

    GameEntity(GameScreen screen, Animation<TextureRegion> animation) {
        this(screen, animation.getKeyFrame(0f));
        this.animation = animation;
    }

    public void setSounds(Audio.Sounds hurtSound, Audio.Sounds death) {
        this.hurtSound = hurtSound;
        this.deathSound = death;
    }

    protected GameEntity(GameScreen screen, TextureRegion keyframe) {
        this.assets = screen.game.assets;
        this.screen = screen;
        this.animation = null;
        this.keyframe = keyframe;
        this.grounded = true;
        this.stateTime = 0f;
        this.healthMeter = new HealthMeter(this);
    }

    protected void setHealth(float hitPoints) {
        this.maxHealth = this.hitPoints = hitPoints;
    }

    protected void setAnimation(Animation<TextureRegion> animation) {
        this.animation = animation;
        stateTime = 0;
    }

    protected void initEntity(float x, float y, float width, float height) {
        collisionBounds.set(x, y, width, height);
        imageBounds.set(this.collisionBounds);
        setPosition(x, y);
    }

    public void changeDirection() {
        setDirection((direction == Direction.left) ? Direction.right : Direction.left);
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void move(Direction direction, float moveSpeed) {
        float speed = direction == Direction.left ? -moveSpeed : moveSpeed;
        this.direction = direction;
        velocity.add(speed, 0);

        if (state != State.jumping) {
            state = State.walking;
        }
    }

    public void update(float dt) {
        if (updateStateTimer()) {
            stateTime += dt;
        }

        if (animation != null) {
            float frameTime = state != State.jumping ? stateTime: 0;
            keyframe = animation.getKeyFrame(frameTime);
        }

        // clamp velocity to maximum, horizontal only
        velocity.x = MathUtils.clamp(velocity.x, -maxHorizontalVelocity, maxHorizontalVelocity);

        if (state != State.jumping) {
            // stop if entity gets slow enough
            if (Math.abs(velocity.x) < 10f) {
                velocity.x = 0f;
                state = State.standing;
            }
        }

        imageBounds.setPosition(position.x - imageBounds.width / 2f, position.y - collisionBounds.height / 2f);
        collisionBounds.setPosition(position.x - collisionBounds.width/2f, position.y - collisionBounds.height/2f);
        collisionCircle.setPosition(position.x, position.y);
        collisionCircle.setRadius(collisionBounds.width / 2f);
        healthMeter.update(dt);
    }

    protected boolean updateStateTimer() {
        return !dead;
    }

    public void updateBounds(){
        collisionBounds.setPosition(position.x - collisionBounds.width/2f, position.y - collisionBounds.height/2f);
    }

    public void centerOn(GameEntity entity) {
        float x = entity.collisionBounds.x + (entity.collisionBounds.width - collisionBounds.width)/2;
        float y = entity.collisionBounds.y + (entity.collisionBounds.height - collisionBounds.height)/2;
        setPosition(x, y);
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
        imageBounds.setPosition(x - imageBounds.width / 2f, y - collisionBounds.height / 2f);
        collisionBounds.setPosition(x - collisionBounds.width/2f, y - collisionBounds.height/2f);
        collisionCircle.setPosition(x, y);
        collisionCircle.setRadius(collisionBounds.width / 2f);
    }

    public void render(SpriteBatch batch) {
        if (keyframe == null) return;

        float scaleX = (direction == Direction.right) ? 1 : -1;
        float scaleY = 1;
//        if (!grounded){
//            scaleX *= .85f;
//            scaleY = 1.15f;
//        }

        batch.setColor(getEffectColor());

        batch.draw(keyframe, imageBounds.x, imageBounds.y,
                imageBounds.width / 2, imageBounds.height / 2,
                imageBounds.width, imageBounds.height, scaleX, scaleY, renderRotation);

        batch.setColor(Color.WHITE);
        // drops have 0 hit points, so this works
        if (hitPoints > 0) {
            healthMeter.render(batch);
        }

        if (Config.debug) {
            batch.setColor(Color.YELLOW);
            assets.debugNinePatch.draw(batch, imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height);
            batch.setColor(Color.RED);
            assets.debugNinePatch.draw(batch, collisionBounds.x, collisionBounds.y, collisionBounds.width, collisionBounds.height);
            batch.setColor(Color.WHITE);
        }
    }

    // override for effects
    public Color getEffectColor() {
        return Color.WHITE;
    }

    public void renderHealthMeter(SpriteBatch batch) {
        healthMeter.render(batch);
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public Vector2 getVelocity() {
        return velocity;
    }

    @Override
    public float getBounceScale() {
        return bounceScale;
    }

    @Override
    public Vector2 getAcceleration() {
        return acceleration;
    }

    @Override
    public Shape2D getCollisionBounds() {
        updateBounds();
        return collisionBounds;
    }

    @Override
    public boolean isGrounded() {
        return grounded;
    }

    @Override
    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }
    
    public long playSound(Audio.Sounds sound) {
        return screen.game.audio.playSound(sound);
    }

    public void takeDamage(float damage) {
        hitPoints = Math.max(0, hitPoints - damage);
        if (hitPoints == 0) {
            dead = true;
            velocity.set(0, 0);
            playSound(deathSound);
        } else {
            playSound(hurtSound);
        }
    }

    public float getHealthPercentage() {
        return hitPoints / maxHealth;
    }

    public boolean isInvulnerable() {
        return false;
    }
}