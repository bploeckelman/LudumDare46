package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld46.screens.GameScreen;

public class Mob extends GameEntity {

    private Array<Animation<TextureRegion>> mobAnimations;
    private TextureRegion[] mobFrames;
    private float[] mobOffset;
    private Rectangle[] mobBounds;

    float mobTime = 0;

    public Mob(GameScreen screen, float x, float y) {
        super(screen, screen.assets.whitePixel);

        int count = MathUtils.random(3, 7);

        mobAnimations = new Array<>(count);
        float width = 0;
        float height = 0;

        mobFrames = new TextureRegion[count];
        mobOffset = new float[count];
        mobBounds = new Rectangle[count];

        TextureRegion frame;
        float dx = 0;
        for (int i = 0; i < count; i++) {
            Animation<TextureRegion> anim = MathUtils.randomBoolean()
                    ? screen.assets.mobTorchAnimation
                    : screen.assets.mobPitchforkAnimation;

            mobAnimations.add(anim);
            mobOffset[i] = MathUtils.random(0f, 2f);
            frame =  anim.getKeyFrame(0);
            mobFrames[i] = frame;

            float fw = frame.getRegionWidth() * 2f;
            float fh = frame.getRegionHeight() * 2f;

            mobBounds[i] = new Rectangle(dx, 0, fw, fh);
            width += dx;
            height = Math.max(height, fh);
            dx += fw/2;
        }
        dx += mobBounds[count-1].width/2;

        initEntity(x, y, dx, height);
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        mobTime += dt;

        for (int i = 0; i < mobFrames.length; i++) {
            mobFrames[i] = mobAnimations.get(i).getKeyFrame(mobTime + mobOffset[i]);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);

        float x = collisionBounds.x;
        float y = collisionBounds.y;

        for (int i = 0; i < mobBounds.length; i++) {
            batch.draw(mobFrames[i], x + mobBounds[i].x, y, mobBounds[i].width/2,
                mobBounds[i].height/2, mobBounds[i].width, mobBounds[i].height, 1, 1, 0);
        }
    }
}
