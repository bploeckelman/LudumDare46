package lando.systems.ld46.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld46.entities.GameEntity;
import lando.systems.ld46.screens.GameScreen;
import lando.systems.ld46.utils.Utils;

public class GuideArrow {
    private static final float width = 150f;
    private static final float height = 150f;

    private TextureRegion icon;
    private float pulseTimer;
    private GameScreen screen;
    private boolean targetOffscreen;
    private Vector2 target;
    private Vector2 position;
    private float x;
    private float y;


    public GuideArrow(GameScreen screen, float x, float y) {
        this.icon = screen.assets.iconArrow;
        this.pulseTimer = 0;
        this.screen = screen;
        this.targetOffscreen = false;
        this.target = new Vector2(x, y);
        this.position = new Vector2();

    }

    public void update(float dt) {
        pulseTimer += dt;
        if (screen.cameraTargetPos.x - screen.hudCamera.viewportWidth / 2 > target.x) {
            x = screen.cameraTargetPos.x - screen.hudCamera.viewportWidth / 2;
            targetOffscreen = true;
        } else if (screen.cameraTargetPos.x + screen.hudCamera.viewportWidth / 2 - 75f < target.x){
            x = screen.cameraTargetPos.x + screen.hudCamera.viewportWidth / 2 - 75f;
            targetOffscreen = true;
        } else {
            x = target.x;
            targetOffscreen = false;
        }
        position.set(x, 500f);
    }

    public void render(SpriteBatch batch) {
        float iconSize = 150f;
        float margin = 50f;
        if (pulseTimer % 1.001f > 0 && targetOffscreen) {
            float pulsePercentage = (pulseTimer % 0.25f) +1f;
            iconSize = iconSize * pulsePercentage;
        }
        //float rotation = position.sub(screen.player.position).angle(Vector2.Y);
        Vector2 tempVec = new Vector2(target.x, target.y);
        float rotation = -tempVec.sub(position).angle(Vector2.Y);
        //batch.draw(icon, position.x, position.y, iconSize, iconSize);

        batch.draw(icon, position.x, position.y, iconSize / 2, iconSize / 2, iconSize, iconSize, 1f, 1f, rotation );
    }
}
