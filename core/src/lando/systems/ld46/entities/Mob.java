package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld46.Audio;
import lando.systems.ld46.screens.GameScreen;

public class Mob extends EnemyEntity {

    private Array<MobEntity> mobEntities;

    // how far the cronies move from him
    public float influenceDistance = 75;

    public Mob(GameScreen screen) {
        // he's small, but scrappy
        super(screen, screen.assets.playerAnimation, 1.50f);

        // he's tougher - kind of
        maxHorizontalVelocity = 300;

        int count = MathUtils.random(4, 7);
        mobEntities = new Array<>(count);

        while (count-- > 0) {
            MobEntity entity = new MobEntity(this);
            mobEntities.add(entity);
        }
        damage = 30;
    }

    @Override
    public void addToScreen(float x, float y) {
        for (MobEntity entity : mobEntities) {
            entity.addToScreen(x, y);
        }
        // add last so it gets drawn over the others
        super.addToScreen(x, y);
    }

    @Override
    public void removeFromScreen() {
        super.removeFromScreen();
        for (MobEntity entity : mobEntities) {
            entity.removeFromScreen();
        }
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        if (mobEntities.size > 0) {
            // you eye balling me punk?
            updateDirection();

            // this checks to remove dead homies - their update is in the game screen
            for (int i = mobEntities.size - 1; i >= 0; i--) {
                MobEntity entity = mobEntities.get(i);
                // this removes from index, actual removal is in game screen
                if (entity.dead) {
                    mobEntities.removeIndex(i);
                }
            }
        } else {
            flee(dt);
        }
    }

    private boolean fled = false;
    // enough time for the dead homies to die
    private float fleeTime = 4;
    private void flee(float dt) {
        if (!fled) {
            spawnDrop();
            screen.physicsEntities.removeValue(this, true);
            changeDirection();
            fled = true;
            playSound(Audio.Sounds.mob_boss_flee);
        }

        float runDist = 300*dt;
        if (direction == Direction.left) {
            runDist = -runDist;
        }

        // make him run away
        setPosition(position.x + runDist, position.y);
        fleeTime -= dt;
        if (fleeTime < 0) {
            removeFromScreen();
        }
    }

    private void updateDirection() {
        Player player = screen.player;
        ZombieMech zombie = screen.zombieMech;

        float x = Float.MAX_VALUE;
        float dx = Float.MAX_VALUE;
        if (zombie != null) {
            x = zombie.position.x;
            dx = Math.abs(position.x - x);
        }
        // player needs to be 2x closer to get this guy's attention
        if (Math.abs(player.position.x - position.x) < (dx / 2)) {
            x = player.position.x;
        }

        direction = (x < position.x) ? Direction.left : Direction.right;
    }

    @Override
    public Color getEffectColor() {
        return Color.GREEN;
    }
}
