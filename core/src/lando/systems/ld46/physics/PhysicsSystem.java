package lando.systems.ld46.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.ObjectMap;
import lando.systems.ld46.screens.GameScreen;
import lando.systems.ld46.utils.Utils;

public class PhysicsSystem {

    static final float GRAVITY = -600;

    private GameScreen screen;
    private Vector2 normal;
    Vector2 tempStart1 = new Vector2();
    Vector2 tempEnd1 = new Vector2();
    Vector2 frameEndPos = new Vector2();
    Vector2 tempStart2 = new Vector2();
    Vector2 tempEnd2 = new Vector2();
    Vector2 nearest1 = new Vector2();
    Vector2 nearest2 = new Vector2();
    Vector2 incomingVector = new Vector2();
    Vector2 nearestRight1 = new Vector2();
    Vector2 nearestRight2 = new Vector2();
    Vector2 collisionResult = new Vector2();
    Vector2 moveVector = new Vector2();
    Vector2 oldPos = new Vector2();

    Array<Collision> collisions = new Array<>();



    public PhysicsSystem(GameScreen screen) {
        this.screen = screen;
        this.normal = new Vector2();
    }

    public void update(float dt) {
        //update particles
        updateParticles(dt);
        updateGameEntities(dt);
    }

    private void updateGameEntities(float dt) {
        Array<PhysicsComponent> entities = screen.physicsEntities;
        for (PhysicsComponent obj : entities) {
            Vector2 accel = obj.getAcceleration();
            Vector2 vel = obj.getVelocity();
            Vector2 pos = obj.getPosition();
            Rectangle bounds = (Rectangle) obj.getCollisionBounds();
            oldPos.set(pos);
            vel.x *= (float)Math.pow(.01f, dt);

            vel.x += accel.x * dt;
            float gravity = obj.isGrounded() ? 0 : GRAVITY;
            vel.y += (accel.y + gravity) * dt;
            moveVector.set(vel.x * dt, vel.y * dt);

            boolean groundThisFrame = false;
            collisions.clear();
            for (Segment2D segment : screen.level.collisionSegments){
                for (int x = 0; x < bounds.width; x += 16){
                    tempStart1.set(bounds.x + x, bounds.y);
                    testCollision(tempStart1, segment, moveVector);
                    tempStart1.set(bounds.x + x, bounds.y + bounds.height);
                    testCollision(tempStart1, segment, moveVector);
                }
                for (int y = 0; y < bounds.height; y+= 16) {
                    tempStart1.set(bounds.x, bounds.y + y);
                    testCollision(tempStart1, segment, moveVector);
                    tempStart1.set(bounds.x + bounds.width, bounds.y + y);
                    testCollision(tempStart1, segment, moveVector);
                }
                tempStart1.set(bounds.x, bounds.y + bounds.height);
                testCollision(tempStart1, segment, moveVector);
                tempStart1.set(bounds.x + bounds.width, bounds.y);
                testCollision(tempStart1, segment, moveVector);
                tempStart1.set(bounds.x + bounds.width, bounds.y + bounds.height);
                testCollision(tempStart1, segment, moveVector);

                for (int x = 0; x < bounds.width; x+= 16) {
                    tempStart1.set(bounds.x + x, bounds.y);
                    if (testGround(tempStart1, segment)) groundThisFrame = true;
                }
                tempStart1.set(bounds.x + bounds.width, bounds.y);
                if (testGround(tempStart1, segment)) groundThisFrame = true;

            }

            collisions.sort();
            for(Collision c : collisions){
                handleCollision(c.startPos, c.segment, moveVector, tempEnd2);
            }

            obj.setGrounded(groundThisFrame);

            pos.add(moveVector);
            vel.set(moveVector.scl(1/dt));

            // Fuck this!!!!1! just push the god damn player out of the fucking ground
            for(Segment2D segment : screen.level.collisionSegments) {
                //bottom
                if (intersectSegments(tempStart1.set(bounds.x, bounds.y), tempEnd1.set(bounds.x + bounds.width, bounds.y), segment.start, segment.end, collisionResult)) {
                    pos.add(segment.normal.x *2f, segment.normal.y * 2f);
                }
                //top
                if (intersectSegments(tempStart1.set(bounds.x, bounds.y + bounds.height), tempEnd1.set(bounds.x + bounds.width, bounds.y + bounds.height), segment.start, segment.end, collisionResult)) {
                    pos.add(segment.normal.x *2f, segment.normal.y * 2f);
                }
                //left
                if (intersectSegments(tempStart1.set(bounds.x, bounds.y), tempEnd1.set(bounds.x, bounds.y + bounds.height), segment.start, segment.end, collisionResult)) {
                    pos.add(segment.normal.x *2f, segment.normal.y * 2f);
                }
                //right
                if (intersectSegments(tempStart1.set(bounds.x + bounds.width, bounds.y), tempEnd1.set(bounds.x + bounds.width, bounds.y + bounds.height), segment.start, segment.end, collisionResult)) {
                    pos.add(segment.normal.x *2f, segment.normal.y * 2f);
                }

                if (bounds.contains(segment.start) && bounds.contains(segment.end)){
                    pos.add(segment.normal.x * 2f, segment.normal.y * 2f);
                }
            }
        }
    }

    private void updateParticles(float dt){
        Array<PhysicsComponent> particles = screen.particles.getPhysicalParticles();
        for(PhysicsComponent obj : particles){
            Vector2 accel = obj.getAcceleration();
            Vector2 vel = obj.getVelocity();
            Vector2 pos = obj.getPosition();
            float scale = obj.getBounceScale();
            Circle bounds = (Circle) obj.getCollisionBounds();

            vel.scl((float)Math.pow(.4f, dt));

            vel.x += accel.x * dt;
            vel.y += (accel.y + GRAVITY) * dt;


            float nextX = pos.x + vel.x * dt;
            float nextY = pos.y + vel.y * dt;
            tempStart1.set(pos);
            tempEnd1.set(nextX, nextY);
            frameEndPos.set(tempEnd1);

            for (Segment2D segment : screen.level.collisionSegments) {
                collisionResult = checkSegmentCollision(tempStart1, tempEnd1, segment.start, segment.end, nearest1, nearest2, collisionResult);
                if (collisionResult.x != Float.MAX_VALUE && nearest1.dst(nearest2) < bounds.radius + 2f){
                    tempEnd1.set(tempStart1.sub(normal.x * bounds.radius, normal.y * bounds.radius));
                    normal.set(segment.end).sub(segment.start).nor().rotate90(1);
                    frameEndPos.set(nearest1);
                    float backupDist = (bounds.radius + 2.1f) - nearest1.dst(nearest2);
                    float x = frameEndPos.x - backupDist * (normal.x);
                    float y = frameEndPos.y - backupDist * (normal.y);
                    frameEndPos.set(x, y);

                    vel.scl(scale);
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

    private void testCollision(Vector2 startPos, Segment2D segment, Vector2 movement) {
        tempStart1.set(startPos.x, startPos.y);
        tempEnd1.set(startPos.x + movement.x, startPos.y + movement.y);
//        collisionResult = checkSegmentCollision(tempStart1, tempEnd1, segment.start, segment.end, nearest1, nearest2, collisionResult);
        if (intersectSegments(tempStart1, tempEnd1, segment.start, segment.end, collisionResult)){
            collisions.add(new Collision(tempStart1, segment, Math.min(collisionResult.dst2(segment.start), collisionResult.dst2(segment.end))));
        }
    }

    private void handleCollision(Vector2 startPos, Segment2D segment, Vector2 movement, Vector2 end) {
        tempStart1.set(startPos.x, startPos.y);
        tempEnd1.set(startPos.x + movement.x, startPos.y + movement.y);
//        collisionResult = checkSegmentCollision(tempStart1, tempEnd1, segment.start, segment.end, nearest1, nearest2, collisionResult);
        if (intersectSegments(tempStart1, tempEnd1, segment.start, segment.end, collisionResult)){
            tempStart1.set(segment.end).sub(segment.start);
            float dot = tempEnd1.set(movement).dot(tempStart1);
            movement.set(tempStart1.scl(dot/ tempStart1.len2()));
        }
    }

    private boolean testGround(Vector2 startPos, Segment2D segment){
        tempStart1.set(startPos.x, startPos.y);
        tempEnd1.set(startPos.x, startPos.y - 5);
//        collisionResult = checkSegmentCollision(tempStart1, tempEnd1, segment.start, segment.end, nearest1, nearest2, collisionResult);
        if (intersectSegments(tempStart1, tempEnd1, segment.start, segment.end, collisionResult)){
            return true;
        }
        return false;
    }

//    private void testDownRay(Vector2 start, )

    Vector2 d1 = new Vector2();
    Vector2 d2 = new Vector2();
    Vector2 r = new Vector2();
    private Vector2 checkSegmentCollision(Vector2 seg1Start, Vector2 seg1End, Vector2 seg2Start, Vector2 seg2End, Vector2 nearestSeg1, Vector2 nearestSeg2, Vector2 results){
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
            return results.set(Float.MAX_VALUE, Float.MAX_VALUE);
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
        return results.set(s, t);
    }

    Vector2 b = new Vector2();
    Vector2 d = new Vector2();
    Vector2 c = new Vector2();
    private boolean intersectSegments(Vector2 start1, Vector2 end1, Vector2 start2, Vector2 end2, Vector2 intersection) {
        intersection.set(0,0);
        b.set(end1).sub(start1);
        d.set(end2).sub(start2);

        float dot = b.x * d.y - b.y * d.x;
        if (dot == 0) return false;

        c.set(start2).sub(start1);
        float t = (c.x * d.y - c.y * d.x) / dot;
        float u = (c.x * b.y - c.y * b.x) / dot;
        if (t < 0 || t > 1 || u < 0 || u > 1) return false;

        intersection.set(start1).add(b.scl(t));
        return true;
    }


}
