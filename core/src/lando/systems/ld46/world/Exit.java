package lando.systems.ld46.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld46.Assets;

public class Exit {

    public Vector2 pos;
    public float size = 32f;
    public TextureRegion texture;
    public Rectangle bounds;

    public Exit(float x, float y, Assets assets) {
        this.pos = new Vector2(x, y);
        this.texture = assets.whitePixel;
        this.bounds = new Rectangle(pos.x, pos.y, size, size);
    }

    public void update() {}

    public void render(SpriteBatch batch) {
        batch.setColor(Color.BLUE);
        batch.draw(texture, pos.x, pos.y, size, size);
        batch.setColor(Color.WHITE);
    }

}
