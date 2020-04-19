package lando.systems.ld46.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import lando.systems.ld46.Config;
import lando.systems.ld46.Game;
import lando.systems.ld46.ui.typinglabel.TypingLabel;

public class EndScreen extends BaseScreen {

    private TypingLabel titleLabel;
    private TypingLabel themeLabel;
    private TypingLabel leftCreditLabel;
    private TypingLabel rightCreditLabel;
    private TypingLabel thanksLabel;
    private TypingLabel disclaimerLabel;


    static String title = "Zombie Prober: {SICK}Necrophelia Edition{ENDSICK}";
    static String theme = "Made for Ludum Dare 46: Keep it alive";
    static String thanks = "Thanks for playing our game!";
    static String developers = "Developed by:\nDoug Graham\nBrian Ploeckelman\nBrian Rossman\nJeffrey Hwang";
    static String artists = "Art by:\nMatt Neumann";
    static String otherDuties = "Other Duties as Assigned:\nLuke Bain";
    static String emotionalSupport = "Emotional Support:\nHamsters";
    static String music = "Sound by:\nSomeone";
    static String libgdx = "Made with {COLOR=red}<3{COLOR=white} and LibGDX";
    static String disclaimer = "Disclaimer!!!\nHamsters were harmed in making of this game.";
    Color textColor = new Color(Color.WHITE);
    Color textBorderColor = new Color(Color.GRAY);

    public EndScreen(Game game) {
        super(game);
        titleLabel = new TypingLabel(assets.riseFont16, title, 0f, Config.windowHeight / 2f + 290f);
        titleLabel.setWidth(Config.windowWidth);
        titleLabel.setFontScale(1.5f);

        themeLabel = new TypingLabel(assets.riseFont16, theme, 0f, Config.windowHeight / 2f + 200f);
        themeLabel.setWidth(Config.windowWidth);
        themeLabel.setFontScale(1f);

        leftCreditLabel = new TypingLabel(assets.riseFont16, developers + "\n\n" + emotionalSupport + "\n\n", 75f, Config.windowHeight / 2f + 155f);
        leftCreditLabel.setWidth(Config.windowWidth / 2 - 150f);
        leftCreditLabel.setLineAlign(Align.left);
        leftCreditLabel.setFontScale(1f);

        rightCreditLabel = new TypingLabel(assets.riseFont16, artists + "\n\n" + music + "\n\n" + otherDuties + "\n\n" + libgdx, Config.windowWidth / 2 + 75f, Config.windowHeight / 2f + 155f);
        rightCreditLabel.setWidth(Config.windowWidth / 2 - 150f);
        rightCreditLabel.setLineAlign(Align.left);
        rightCreditLabel.setFontScale(1f);

        thanksLabel = new TypingLabel(assets.riseFont16, thanks, 0f, 125f);
        thanksLabel.setWidth(Config.windowWidth);
        thanksLabel.setLineAlign(Align.center);
        thanksLabel.setFontScale(1f);

        disclaimerLabel = new TypingLabel(assets.riseFont16, "{JUMP=.2}{WAVE=0.9;1.2;1.75}{RAINBOW}" + disclaimer + "{ENDRAINBOW}{ENDWAVE}{ENDJUMP}", 0f, 75f);
        disclaimerLabel.setWidth(Config.windowWidth);
        thanksLabel.setLineAlign(Align.center);
        disclaimerLabel.setFontScale(.75f);


    }

    @Override
    public void update(float dt) {
        titleLabel.update(dt);
        themeLabel.update(dt);
        leftCreditLabel.update(dt);
        rightCreditLabel.update(dt);
        thanksLabel.update(dt);
        disclaimerLabel.update(dt);

        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            game.setScreen(new TitleScreen(game));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.begin();
        batch.setProjectionMatrix(worldCamera.combined);
        batch.setColor(0f, 0f, 0f, 0.6f);
        batch.draw(assets.whitePixel, 25f, 110f, Config.windowWidth / 2 - 50f, 350f);
        batch.draw(assets.whitePixel, Config.windowWidth / 2 + 25f, 110f, Config.windowWidth / 2 - 50f, 350f);
        batch.setColor(Color.WHITE);
        titleLabel.render(batch);
        themeLabel.render(batch);
        leftCreditLabel.render(batch);
        rightCreditLabel.render(batch);
        thanksLabel.render(batch);
        disclaimerLabel.render(batch);
        batch.end();
    }

}
