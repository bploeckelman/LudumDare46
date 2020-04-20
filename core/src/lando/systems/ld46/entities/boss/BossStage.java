package lando.systems.ld46.entities.boss;

public interface BossStage {
    void update(float dt);
    boolean isComplete();
    BossStage nextStage();
}
