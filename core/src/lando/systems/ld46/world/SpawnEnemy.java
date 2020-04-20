package lando.systems.ld46.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld46.Game;
import lando.systems.ld46.entities.*;
import lando.systems.ld46.screens.GameScreen;

public class SpawnEnemy {

    public Game game;
    public Vector2 pos;
    public float size = Level.TILE_SIZE;

    public TextureRegion texture;

    public EnemyType enemyType;

    private int maxSpawn;
    private float spawnRate;
    private float timer;

    private Array<EnemyEntity> spawnEntities;

    public SpawnEnemy(Game game, EnemyType enemyType, float x, float y, int maxSpawn, float spawnRate) {
        this.texture = game.assets.whitePixel;

        this.game = game;

        this.enemyType = enemyType;
        this.pos = new Vector2(x, y);
        this.maxSpawn = maxSpawn;
        this.spawnRate = spawnRate;

        spawnEntities = new Array<>();
    }

    public void render(SpriteBatch batch) {
        batch.setColor(1f, 0f, 0f, 0.5f);
        batch.draw(texture, pos.x, pos.y, size, size);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void update(GameScreen screen, float dt) {
        addEnemies(screen, dt);
        removeDeadEnemies();
    }

    private void addEnemies(GameScreen screen, float dt) {
        timer += dt;
        if (timer > spawnRate) {
            if (spawnEntities.size < maxSpawn) {
                spawnEnemy(screen);
            }

            timer = 0;
        }
    }

    private void spawnEnemy(GameScreen screen) {
        EnemyEntity enemy = null;
        switch (this.enemyType) {
            case bat:
            case skeleton: // skeletons are bats
                enemy = new Bat(screen);
                break;
            case snake:
                enemy = new Snek(screen);
                break;
            case mob:
                enemy = new Mob(screen);
                break;
            case mummy:
                enemy = new Mummy(screen);
                break;
        }

        if (enemy == null) return;

        enemy.addToScreen(pos.x, pos.y + size);
        spawnEntities.add(enemy);
    }

    public void removeDeadEnemies() {
        for (int i = spawnEntities.size - 1; i >= 0; i--) {
            if (spawnEntities.get(i).dead) {
                spawnEntities.removeIndex(i);
            }
        }
    }
}
