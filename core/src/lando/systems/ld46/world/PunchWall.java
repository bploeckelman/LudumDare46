package lando.systems.ld46.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld46.Assets;
import lando.systems.ld46.entities.GameEntity;

public class PunchWall {

    public Vector2 pos;
    public TextureRegion texture;
    public Rectangle bounds;
    public boolean dead;
    public GameEntity.Direction punchedDir;

    public PunchWall(float x, float y, Assets assets) {
        this.pos = new Vector2(x, y);
        this.texture = assets.punchWall1x4;
        this.bounds = new Rectangle(pos.x, pos.y, texture.getRegionWidth(), texture.getRegionHeight());
        this.dead = false;
        this.punchedDir = null;
    }

    /**
     * @param punchedDir negative for a punch towards the left, positive for a punch towards the right
     */
    public void punch(GameEntity.Direction punchedDir) {
        this.dead = true;
        this.punchedDir = punchedDir;
    }

    public void render(SpriteBatch batch) {
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        batch.setColor(1f, 1f, 1f, 1f);
    }

}
