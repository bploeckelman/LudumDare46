package lando.systems.ld46.entities.boss;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld46.Game;
import lando.systems.ld46.entities.EnemyEntity;
import lando.systems.ld46.entities.GameEntity;
import lando.systems.ld46.entities.MoveEntity;
import lando.systems.ld46.screens.EndScreen;
import lando.systems.ld46.screens.GameScreen;

public class Boss {

    GameScreen screen;

    public float damage = 10f;

    BossStage currentStage;
    Vector3 position;
    public boolean alive;
    float accum = 0;
    public Rectangle collisionBounds;
    public Rectangle leftBounds;
    public Rectangle rightBounds;
    public Animation<TextureRegion> currentAnimation;
    public TextureRegion keyframe;
    GameEntity.Direction direction;
    int hits;
    int lastPunchIndex;
    boolean flashRed = false;

    public Boss(GameScreen screen) {
        this.screen = screen;
        this.position = new Vector3(screen.worldCamera.viewportWidth/2, screen.worldCamera.viewportHeight/3f*2f, 0);
        currentStage = new BossStage3(this);
        this.alive = true;
        collisionBounds= new Rectangle(this.position.x - 75, this.position.y , 150, 150);
        leftBounds = new Rectangle(this.position.x - 220, this.position.y - 200, 200, 300);
        rightBounds = new Rectangle(this.position.x + 20, this.position.y - 200, 200, 300);
        currentAnimation = screen.assets.bossWalkAnimation;
        keyframe = currentAnimation.getKeyFrame(.01f);
        this.direction = GameEntity.Direction.right;
        hits = 0;
        lastPunchIndex = -1;
    }

    public void update(float dt) {
        keyframe = currentAnimation.getKeyFrame(accum);
        accum += dt;
        if (!alive) return;
        currentStage.update(dt);
        if (currentStage.isComplete()){
            currentStage = currentStage.nextStage();
            if (currentStage == null){
                alive = false;
                screen.game.setScreen(new EndScreen(screen.game));
            }
        }
        if (currentAnimation != screen.assets.bossWalkAnimation) {
            if (accum > currentAnimation.getAnimationDuration()){
                if (currentAnimation == screen.assets.bossHurtAnimation){
                    if (screen.zombieMech != null && collisionBounds.overlaps(screen.zombieMech.collisionBounds)){
                        screen.zombieMech.impulse.set((screen.zombieMech.position.x > position.x) ? 800 : -800, 20, .15f);
                    }
                }
                currentAnimation = screen.assets.bossWalkAnimation;
            }
        }

        if (currentAnimation == screen.assets.bossPunchAnimation){
            int index = currentAnimation.getKeyFrameIndex(accum);
            if (index == 2 && lastPunchIndex != index){
                lastPunchIndex = index;
                Rectangle bounds = direction == GameEntity.Direction.left ? leftBounds : rightBounds;
                MoveEntity p = screen.player;
                if (screen.player.inMech()) p = screen.zombieMech;
                if (bounds.overlaps(p.collisionBounds) && !(p.invulnerabilityTimer > 0) && !screen.buildingMech){
                    p.impulse.set(direction == GameEntity.Direction.right ? 800 : -800, 100, .15f);
                    p.takeDamage(damage);
                    p.invulnerabilityTimer = 2f;
                }
            }
        }

        if (screen.player.dead) {
            screen.enemies.clear();
        }

    }

    public void render(SpriteBatch batch) {
        if (position.z == 0) return;
        float scale = 1f + position.z;
        float width = 200 * scale;
        float height = 600 * scale;
        if (direction == GameEntity.Direction.left) width *= -1;
        float c = position.z * .9f;
        batch.setColor(c, c * (flashRed? 0: 1f), c * (flashRed? 0: 1f), 1f);
        batch.draw(keyframe, position.x - width/2, position.y - height/2, width , height);
        batch.setColor(Color.WHITE);
//        screen.assets.debugNinePatch.draw(batch, collisionBounds.x, collisionBounds.y, collisionBounds.width, collisionBounds.height);
//        screen.assets.debugNinePatch.draw(batch, leftBounds.x, leftBounds.y, leftBounds.width, leftBounds.height);
//        screen.assets.debugNinePatch.draw(batch, rightBounds.x, rightBounds.y, rightBounds.width, rightBounds.height);
    }

    public void takeDamage(GameEntity.Direction direction, float damage) {
        if (currentAnimation == screen.assets.bossHurtAnimation || position.z < 1f) return;
        this.direction = direction;
        accum = 0;
        currentAnimation = screen.assets.bossHurtAnimation;
        hits++;
    }
}
