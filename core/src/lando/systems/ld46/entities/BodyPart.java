package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lando.systems.ld46.Config;
import lando.systems.ld46.screens.GameScreen;
import lando.systems.ld46.ui.GuideArrow;

public class BodyPart extends GameEntity {

    public enum Type {
          arm1
        , arm2
        , leg1
        , leg2
        , head
        ;
        public TextureRegion texture; // initialized in Assets
    }

    public Type type;
    public boolean collected;
    public GuideArrow guideArrow;

    public BodyPart(GameScreen screen, Type type, float x, float y) {
        super(screen, type.texture);
        this.type = type;
        this.collected = false;
        this.maxHorizontalVelocity = 2000;
        float scale = 2f;
        initEntity(x, y, type.texture.getRegionWidth() * scale, type.texture.getRegionHeight() * scale);
        guideArrow = new GuideArrow(screen, x, y);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (collected) {
            guideArrow.show = false;
        } else {
            guideArrow.show = true;
        }
        guideArrow.setTargetPosition(position.x, position.y);
        guideArrow.update(dt);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (keyframe == null) return;
        if (Config.debug) {
            batch.setColor(Color.YELLOW);
            assets.debugNinePatch.draw(batch, collisionBounds.x, collisionBounds.y, collisionBounds.width, collisionBounds.height);
            batch.setColor(Color.WHITE);
        }

        if (!collected) {
            guideArrow.render(batch);
        }

        float scaleX = 1f;
        float scaleY = 1f;

        batch.setColor(Color.WHITE);
        batch.draw(keyframe, imageBounds.x, imageBounds.y,
                imageBounds.width / 2, imageBounds.height / 2,
                imageBounds.width, imageBounds.height, scaleX, scaleY, 0);
    }


}
