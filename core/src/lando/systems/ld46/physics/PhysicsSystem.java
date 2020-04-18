package lando.systems.ld46.physics;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld46.screens.GameScreen;
import lando.systems.ld46.utils.Utils;

public class PhysicsSystem {

    static final float GRAVITY = -300;

    private GameScreen screen;
    private Vector2 normal;
    Vector2 tempStart1 = new Vector2();
    Vector2 tempEnd1 = new Vector2();
    Vector2 frameEndPos = new Vector2();
    Vector2 tempStart2 = new Vector2();
    Vector2 tempEnd2 = new Vector2();
    Vector2 frameVel1 = new Vector2();
    Vector2 frameVel2 = new Vector2();
    Vector2 nearest1 = new Vector2();
    Vector2 nearest2 = new Vector2();
    Vector2 incomingVector = new Vector2();


    public PhysicsSystem(GameScreen screen) {
        this.screen = screen;
        this.normal = new Vector2();
    }

    public void update(float dt) {
        //update particles
        Array<PhysicsComponent> particles = screen.particles.getPhysicalParticles();
        for(PhysicsComponent obj : particles){
            Vector2 accel = obj.getAcceleration();
            Vector2 vel = obj.getVelocity();
            Vector2 pos = obj.getPosition();
            Circle bounds = (Circle) obj.getCollisionBounds();

            vel.x += accel.x * dt;
            vel.y += (accel.y + GRAVITY) * dt;
            vel.scl((float)Math.pow(.8f, dt));

            float nextX = pos.x + vel.x * dt;
            float nextY = pos.y + vel.y * dt;
            tempStart1.set(pos);
            tempEnd1.set(nextX, nextY);
            frameEndPos.set(tempEnd1);

            for (Segment2D segment : screen.level.collisionSegments) {

                float t = checkSegmentCollision(tempStart1, tempEnd1, segment.start, segment.end, nearest1, nearest2);
                if (t != Float.MAX_VALUE && nearest1.dst(nearest2) < bounds.radius + 2f){
                    tempEnd1.set(tempStart1.sub(normal.x * bounds.radius, normal.y * bounds.radius));
                    normal.set(segment.end).sub(segment.start).nor().rotate90(1);
                    frameEndPos.set(nearest1);
                    float backupDist = (bounds.radius + 2.1f) - nearest1.dst(nearest2);
                    float x = frameEndPos.x - backupDist * (normal.x);
                    float y = frameEndPos.y - backupDist * (normal.y);
                    frameEndPos.set(x, y);

                    vel.scl(.8f);
                    if (nearest2.epsilonEquals(segment.start) || nearest2.epsilonEquals(segment.end)){
                        normal.set(nearest2).sub(frameEndPos).nor();
                    } else {
                        normal.set(segment.end).sub(segment.start).nor().rotate90(1);
                    }
                    vel.set(Utils.reflectVector(incomingVector.set(vel), normal));
                }
            }

            pos.set(frameEndPos);


        }


    }





    Vector2 d1 = new Vector2();
    Vector2 d2 = new Vector2();
    Vector2 r = new Vector2();
    private float checkSegmentCollision(Vector2 seg1Start, Vector2 seg1End, Vector2 seg2Start, Vector2 seg2End, Vector2 nearestSeg1, Vector2 nearestSeg2){
        d1.set(seg1End).sub(seg1Start);
        d2.set(seg2End).sub(seg2Start);
        r.set(seg1Start).sub(seg2Start);

        float a = d1.dot(d1);
        float e = d2.dot(d2);
        float f = d2.dot(r);

        float b = d1.dot(d2);
        float c = d1.dot(r);

        float s = 0;
        float t = 0;

        float denom = a*e-b*b;
        if (denom != 0){
            s = MathUtils.clamp((b*f - c*e)/denom, 0f, 1f);
        } else {
            // Parallel
            return Float.MAX_VALUE;
        }

        t = (b*s + f) /e;
        if (t < 0) {
            t = 0;
            s = MathUtils.clamp(-c /a, 0, 1);
        } else if (t > 1) {
            t = 1;
            s = MathUtils.clamp((b-c)/a, 0, 1);
        }

        nearestSeg1.set(seg1Start).add(d1.scl(s));
        nearestSeg2.set(seg2Start).add(d2.scl(t));
        return s;
    }

}
