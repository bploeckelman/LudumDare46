package lando.systems.ld46.entities;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.ld46.screens.GameScreen;

public class BodyBag {

    private final GameScreen screen;

    public ObjectMap<BodyPart.Type, BodyPart> bodyParts;
    public boolean allPartsCollected;

    public BodyBag(GameScreen screen, ObjectMap<BodyPart.Type, Vector2> initialPositions) {
        this.screen = screen;

        this.bodyParts = new ObjectMap<>();
        if (initialPositions.size != BodyPart.Type.values().length) {
            Gdx.app.log("Warning", "BodyBag initial positions invalid");
        }
        for (BodyPart.Type type : BodyPart.Type.values()) {
            Vector2 pos = initialPositions.get(type);
            if (pos == null) {
                Gdx.app.log("Warning", "BodyBag missing initial position for body part '" + type.name() + "'");
                continue;
            }
            BodyPart part = new BodyPart(screen, type, pos.x, pos.y);
            this.bodyParts.put(type, part);
            this.screen.physicsEntities.add(part);
        }
        this.allPartsCollected = false;
    }

    public void update(float dt, Player player) {
        boolean allPartsWereAlreadyCollected = allPartsCollected;
        allPartsCollected = true;
        for (BodyPart part : bodyParts.values()) {
            part.update(dt);
            if (part.collected) {
                continue;
            } else {
                if (!player.freeze && player.collisionBounds.overlaps(part.collisionBounds)) {
                    part.collected = true;
                    screen.particles.spawnBodyPartPickup(part.position.x, part.position.y);
                    screen.physicsEntities.removeValue(part, true);
                }
            }
            if (!part.collected) {
                allPartsCollected = false;
            }
        }
        // Just collected all parts, tell the gamescreen to trigger the mech rebuild
        if (allPartsCollected && !allPartsWereAlreadyCollected) {
            screen.buildZombieMech();
        }
    }

    public void render(SpriteBatch batch) {
        for (BodyPart part : bodyParts.values()) {
            if (part.collected) continue;
            part.render(batch);
        }
    }

    public void explodeParts(float x, float y) {
        screen.player.freeze = true;
        Timeline.createSequence()
                .delay(1.5f)
                .push(Tween.call((type, source) -> {
                    screen.player.freeze = false;
                }))
                .start(screen.game.tween);

        for (BodyPart part : bodyParts.values()) {
            part.collected = false;
            part.setPosition(x, y);
            float velX = (GameEntity.Direction.random() == GameEntity.Direction.left)
                       ? MathUtils.random(-2000f, -1000f)
                       : MathUtils.random(1000f, 2000);
            float velY = MathUtils.random(800f, 1000f);
            part.velocity.add(velX, velY);
            screen.physicsEntities.add(part);
        }
    }

}
