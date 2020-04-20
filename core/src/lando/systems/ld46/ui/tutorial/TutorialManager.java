package lando.systems.ld46.ui.tutorial;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import lando.systems.ld46.screens.GameScreen;
import lando.systems.ld46.world.LevelDescriptor;

public class TutorialManager {

    Array<TutorialSection> sections;
    TutorialSection activeSection;

    public TutorialManager(GameScreen screen) {
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

}
