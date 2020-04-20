package lando.systems.ld46.ui.tutorial;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Bounce;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld46.accessors.RectangleAccessor;
import lando.systems.ld46.entities.GameEntity;
import lando.systems.ld46.screens.GameScreen;
import lando.systems.ld46.ui.typinglabel.TypingLabel;

public class TutorialSection {

    public TutorialStartTrigger trigger;
    public String text;
    public Array<GameEntity> entitiesToArrow;
    public GameScreen screen;
    public boolean shouldBlockInput;
    public boolean finished;
    public Rectangle bounds;
    public boolean ready;
    public float delay;
    float accum;

    TypingLabel typingLabel;

    public TutorialSection(GameScreen screen, TutorialStartTrigger trigger, Array<GameEntity> entitiesToArrow, String text, boolean shouldBlockInput) {
        this.trigger = trigger;
        this.text = text;
        this.entitiesToArrow = entitiesToArrow;
        if (this.entitiesToArrow == null) this.entitiesToArrow = new Array<>();
        this.screen = screen;
        this.shouldBlockInput = shouldBlockInput;
        this.finished = false;
        this.bounds = new Rectangle();
        this.ready = false;
        delay = 0;
        accum = 0;
    }

    public void update(float dt) {
        accum += dt;
        if (typingLabel != null) {
            typingLabel.update(dt);
            if (shouldBlockInput) {
                if (Gdx.app.getInput().justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    if (!typingLabel.hasEnded()) {
                        typingLabel.skipToTheEnd();
                    } else {
                        finished = true;
                    }
                }
            }
        }


    }

    public void render(SpriteBatch batch) {
        batch.setColor(Color.DARK_GRAY);
        batch.draw(screen.assets.whitePixel, bounds.x +1, bounds.y +1, bounds.width -2, bounds.height -2);
        batch.setColor(Color.WHITE);
        screen.assets.tutorialNinePatch.draw(batch, bounds.x, bounds.y, bounds.width, bounds.height);
        if (typingLabel != null) {
            typingLabel.render(batch);
            if (typingLabel.hasEnded()){
                GlyphLayout layout = screen.assets.layout;
                BitmapFont font = screen.assets.pixelFont16;
                font.setColor(1f, 1f, 1f, Math.abs(MathUtils.sin(accum * 4f)));
                layout.setText(font, "Press Enter or Click");
                font.draw(batch, layout, bounds.x + bounds.width - 10 - layout.width, bounds.y + 10 + layout.height);
                font.setColor(Color.WHITE);
            }
        }
    }

    public void activate(){
        // if you need to do something when it starts

        Timeline.createSequence()
                .push(Tween.set(bounds, RectangleAccessor.XY).target(screen.hudCamera.viewportWidth/2f, screen.hudCamera.viewportHeight - 300 + 110))
                .pushPause(delay)
                .push(Tween.to(bounds, RectangleAccessor.XYWH, 1f)
                        .target(80, screen.hudCamera.viewportHeight - 300, screen.hudCamera.viewportWidth - 160, 220)
                        .ease(Bounce.OUT))
                .push(Tween.call((type, source) -> {
                        ready = true;
                        typingLabel = new TypingLabel(screen.assets.riseFont16, text, 90, screen.hudCamera.viewportHeight - 90);
                        typingLabel.setWidth(screen.hudCamera.viewportWidth - 180);
                        typingLabel.setFontScale(.8f);
                    }))
                .start(screen.game.tween);
    }

    public boolean checkTrigger(float dt){
        if (trigger == null) return true;
        return trigger.check(dt);
    }
}
