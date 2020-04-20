package lando.systems.ld46.entities;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.ld46.Audio;
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
                    player.playSound(Audio.Sounds.pickup_flesh);
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
            player.playSound(Audio.Sounds.assemble_zombie);
        }
    }

    public void render(SpriteBatch batch) {
        for (BodyPart part : bodyParts.values()) {
            if (part.collected) continue;
            part.render(batch);
        }
    }

    // Down apparently? can't figure out which winding MathUtils trig functions use....
//    private static final float[] explodeAngles = new float[] { 202.5f, 247.5f, 270f, 292.5f, 337.5f };
    private static final float[] explodeAngles = new float[] { 22.5f, 67.5f, 90f, 112.5f, 157.5f };
    public void explodeParts(float x, float y) {
        screen.player.freeze = true;
        Timeline.createSequence()
                .delay(1.5f)
                .push(Tween.call((type, source) -> {
                    screen.player.freeze = false;
                }))
                .start(screen.game.tween);

        int i = 0;
        for (BodyPart part : bodyParts.values()) {
            part.collected = false;
            part.setPosition(x, y);
            float angle = explodeAngles[i++];
            float speed = MathUtils.random(1000f, 2000f);
            float velX = MathUtils.cosDeg(angle) * speed;
            float velY = MathUtils.sinDeg(angle) * speed;
            part.velocity.add(velX, velY);
            screen.physicsEntities.add(part);
        }
    }

}
