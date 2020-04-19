package lando.systems.ld46.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld46.Assets;

public class PunchWall {

    public Vector2 pos;
    public Vector2 size;
    public float width = Level.TILE_SIZE;
    public float height = 6 * Level.TILE_SIZE;
    public TextureRegion texture;

    public PunchWall(float x, float y, float w, float h, Assets assets) {
        this.pos = new Vector2(x, y);
        this.size = new Vector2(w, h);
        this.texture = assets.whitePixel;
    }

    public void render(SpriteBatch batch) {
        batch.setColor(0f, 1f, 0f, 0.5f);
        batch.draw(texture, pos.x, pos.y, width, height);
        batch.setColor(1f, 1f, 1f, 1f);
    }

}
