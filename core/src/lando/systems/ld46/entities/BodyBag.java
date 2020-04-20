package lando.systems.ld46.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
        allPartsCollected = true;
        for (BodyPart part : bodyParts.values()) {
            part.update(dt);
            if (part.collected) {
                allPartsCollected = false;
            } else {
                if (player.collisionBounds.overlaps(part.collisionBounds)) {
                    part.collected = true;
                    screen.particles.spawnBodyPartPickup(part.position.x, part.position.y);
                }
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (BodyPart part : bodyParts.values()) {
            if (part.collected) continue;
            part.render(batch);
        }
    }

}
