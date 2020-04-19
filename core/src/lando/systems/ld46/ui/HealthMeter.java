package lando.systems.ld46.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import lando.systems.ld46.entities.GameEntity;
import lando.systems.ld46.utils.Utils;


public class HealthMeter {

    private static final float width = 100f;
    private static final float height = 5f;

    private Color color;
    private TextureRegion icon;
    private float healthPercentage;
    private float pulseTimer;
    private Rectangle bounds;
    private GameEntity entity;

    public HealthMeter(GameEntity entity) {
        this.icon = entity.screen.assets.iconHeart;
        this.healthPercentage = 1;
        this.bounds = new Rectangle();
        this.pulseTimer = 0;
        this.entity = entity;
    }

    public void update(float dt) {

        healthPercentage = entity.getHealthPercentage();
        bounds.set(entity.position.x - entity.collisionBounds.width / 2, entity.position.y + entity.imageBounds.height / 2 + 5f, entity.collisionBounds.width, height);
        pulseTimer += dt;
    }

    public void render(SpriteBatch batch) {
        batch.setColor(Color.BLACK);
        batch.draw(entity.screen.assets.whitePixel, bounds.x, bounds.y, bounds.width, bounds.height);

        color = Utils.hsvToRgb(((healthPercentage * 120f) - 20) / 365f, 1.0f, 1.0f, color);
        batch.setColor(color);
        batch.draw(entity.screen.assets.whitePixel, bounds.x, bounds.y, bounds.width * healthPercentage, bounds.height);

        batch.setColor(Color.WHITE);
        entity.screen.assets.debugNinePatch.draw(batch, bounds.x, bounds.y, bounds.width, bounds.height);

        float iconSize = icon.getRegionHeight();
        if (pulseTimer % 1.1f > (healthPercentage)) {
            float pulsePercentage = (pulseTimer % 0.25f) +1f;
            iconSize = iconSize * pulsePercentage;
        }
        batch.draw(icon, bounds.x - bounds.width / 2 - iconSize / 2f, bounds.y + bounds.height / 2f - iconSize / 2f, iconSize, iconSize);
    }

}
