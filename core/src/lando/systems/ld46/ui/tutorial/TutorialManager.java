package lando.systems.ld46.ui.tutorial;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import lando.systems.ld46.screens.GameScreen;
import lando.systems.ld46.world.LevelDescriptor;

public class TutorialManager {

    Array<TutorialSection> sections;
    TutorialSection activeSection;

    private GameScreen screen;

    public TutorialManager(GameScreen screen) {
        this.screen = screen;
        sections = new Array<>();
        I18NBundle texts = screen.assets.tutorialText;

        // TODO: build tutorial shit based on which level is loaded
        if (screen.level.thisLevel == LevelDescriptor.level_tutorial) {
            TutorialSection section1 = new TutorialSection(screen, null, null, texts.get("tutorial_1"), true);
            section1.delay = 3f;
            sections.add(section1);
        }
    }

    public void update(float dt) {
        if (activeSection != null){
            activeSection.update(dt);
            if (activeSection.finished) {
                activeSection = null;
            }
        } else {
            if (sections.size <= 0) return;
            TutorialSection nextSection = sections.get(0);
            if (nextSection.checkTrigger(dt)){
                activeSection = nextSection;
                activeSection.activate();
                activeSection.update(dt);
                sections.removeIndex(0);
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (activeSection != null){
            activeSection.render(batch);
        }
    }

    public boolean shouldBlockInput() {
        if (activeSection != null) return activeSection.shouldBlockInput;
        return false;
    }

    public void showInstantMessage(String message) {
        TutorialSection instaMessage = new TutorialSection(screen, null, null, message, false);
        sections.add(instaMessage);
    }

    private String[] deathMessage = {
            "You died, but hey - it's Ludum Dare. This one is on us.",
            "You have died, again",
            "This is getting ridiculous",
            "Are you even trying?",
            "This is the last one",
            "Ok, this is",
            "Alright, you found the loophole",
            "There are no more death messages",
            "Really, that's it",
            "Now you are just looking for witty banter",
            "Have you tried going outside?",
            "404 - death message not found"
    };

    // could be in a stats object, but fuck it - it's a dare
    private int deathCount = 0;

    public void showDeathMessage() {
        int index = Math.min(deathMessage.length - 1, deathCount++);
        showInstantMessage(deathMessage[index]);
    }
}
