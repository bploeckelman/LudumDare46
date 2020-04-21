package lando.systems.ld46.ui.tutorial;

import lando.systems.ld46.screens.GameScreen;

public class TutorialHasPartsTrigger implements TutorialStartTrigger {
    GameScreen screen;

    public TutorialHasPartsTrigger(GameScreen screen){
        this.screen = screen;
    }
    @Override
    public boolean check(float dt) {
        return screen.zombieMech != null;
    }
}
