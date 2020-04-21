package lando.systems.ld46.ui.tutorial;

import lando.systems.ld46.screens.GameScreen;

public class TutorialInMechTrigger implements TutorialStartTrigger {
    GameScreen screen;

    public TutorialInMechTrigger(GameScreen screen){
        this.screen = screen;
    }
    @Override
    public boolean check(float dt) {
        return screen.player.inMech(); }
}
