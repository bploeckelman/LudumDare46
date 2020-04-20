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
    private static final float arrowSize = 50f;

    private TextureRegion icon;
    private float pulseTimer;
    private GameScreen screen;
    private boolean targetOffscreenInX;
    private boolean targetOffscreenInY;
    private Vector2 target;
    private Vector2 position;
    private float x;
    private float y;
    public boolean show;


    public GuideArrow(GameScreen screen, float x, float y) {
        this.icon = screen.assets.iconArrow;
        this.pulseTimer = 0;
        this.screen = screen;
        this.targetOffscreenInX = false;
        this.targetOffscreenInY = false;
        this.target = new Vector2(x, y);
        this.position = new Vector2(x, y);
        this.show = false;

    }

    public void setTargetPosition(float x, float y) {
        this.target.set(x, y);
    }

    public void update(float dt) {
        pulseTimer += dt;
        if (screen.cameraTargetPos.x - screen.hudCamera.viewportWidth / 2 + arrowSize + 50f  > target.x) {
            x = screen.cameraTargetPos.x - screen.hudCamera.viewportWidth / 2 + arrowSize + 50f;
            targetOffscreenInX = true;
        } else if (screen.cameraTargetPos.x + screen.hudCamera.viewportWidth / 2 - arrowSize - 50f < target.x){
            x = screen.cameraTargetPos.x + screen.hudCamera.viewportWidth / 2 - arrowSize - 50f;
            targetOffscreenInX = true;
        } else {
            x = target.x;
            targetOffscreenInX = false;
        }

        if (screen.cameraTargetPos.y - screen.hudCamera.viewportHeight / 2 + arrowSize > target.y) {
            y = screen.cameraTargetPos.y - screen.hudCamera.viewportHeight / 2 + arrowSize;
            targetOffscreenInY = true;
        } else if (screen.cameraTargetPos.y + screen.hudCamera.viewportHeight / 2 - arrowSize * 2 < target.y){
            y = screen.cameraTargetPos.y + screen.hudCamera.viewportHeight / 2 - arrowSize * 2;
            targetOffscreenInY = true;
        } else {
            y = target.y;
            targetOffscreenInY = false;
        }
        position.set(x, y + arrowSize / 2);
    }

    public void render(SpriteBatch batch) {
        if (show) {
            float iconSize = arrowSize;
            if (pulseTimer % 1.001f > 0 && !targetOffscreenInY && !targetOffscreenInX) {
                float pulsePercentage = (pulseTimer % 0.25f) +1f;
                iconSize = iconSize * pulsePercentage;
            }
            //float rotation = position.sub(screen.player.position).angle(Vector2.Y);
            Vector2 tempVec = new Vector2(target.x, target.y);
            float rotation = -tempVec.sub(position).angle(Vector2.Y);
            //batch.draw(icon, position.x, position.y, iconSize, iconSize);
            batch.draw(icon, position.x - iconSize / 2f, position.y, iconSize / 2, iconSize, iconSize, iconSize, 1f, 1f, rotation );
        }
    }
}
