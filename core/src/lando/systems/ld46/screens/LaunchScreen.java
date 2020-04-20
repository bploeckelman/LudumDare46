package lando.systems.ld46.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lando.systems.ld46.Audio;
import lando.systems.ld46.Config;
import lando.systems.ld46.Game;
import lando.systems.ld46.ui.typinglabel.TypingLabel;

public class LaunchScreen extends BaseScreen {

    private TypingLabel titleLabel;
    static String title = "{JUMP=.2}{WAVE=0.9;1.2;1.75}{RAINBOW}CLICK TO LAUNCH{ENDRAINBOW}{ENDWAVE}{ENDJUMP}";

    public LaunchScreen(Game game) {
        super(game);
        titleLabel = new TypingLabel(assets.riseFont16, title, 0f, Config.windowHeight / 2f + 50f);
        titleLabel.setWidth(Config.windowWidth);
        titleLabel.setFontScale(2.5f);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (Gdx.input.justTouched()){
            game.setScreen(new TitleScreen(game), assets.doorwayShader, 3f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)){
            game.audio.stopMusic();
            game.setScreen(new EndScreen(game), assets.cubeShader, 3f);
        }
        titleLabel.update(dt);
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.begin();
        batch.setProjectionMatrix(shaker.getCombinedMatrix());

        batch.setColor(Color.BLACK);
        batch.draw(assets.whitePixel, 0,0, shaker.getViewCamera().viewportWidth, shaker.getViewCamera().viewportHeight);
        titleLabel.render(batch);

        batch.end();
    }
}
