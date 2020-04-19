package lando.systems.ld46.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import lando.systems.ld46.Assets;
import lando.systems.ld46.Config;
import lando.systems.ld46.physics.Segment2D;
import lando.systems.ld46.utils.Utils;

public class Feeler {

    Assets assets;
    GameEntity owner;
    Segment2D segment;
    float length;
    float offset;
    boolean isSafe;


    public Feeler(GameEntity owner, Assets assets, float offset, float length){
        this.owner = owner;
        this.assets = assets;
        this.segment = new Segment2D();
        this.offset = offset;
        this.length = length;
        this.isSafe = false;
    }

    public void update(float dt){
        segment.setStart(owner.position.x + offset, owner.collisionBounds.y + 20);
        segment.setEnd(owner.position.x + offset, owner.collisionBounds.y + 20 - length);

        isSafe = owner.screen.physicsSystem.isPositionAboveGround(segment.start, length);
    }

    public void render(SpriteBatch batch){
        if (Config.debug) {
            Utils.drawSegment(assets, segment, 1, isSafe ? Color.YELLOW : Color.RED);
        }
    }
}
