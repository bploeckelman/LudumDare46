package lando.systems.ld46.physics;

import com.badlogic.gdx.math.Vector2;

public class Collision implements Comparable {
    public float distance;
    public Segment2D segment;
    public Vector2 startPos;

    public Collision(Vector2 startPos, Segment2D segment, float dist) {
        this.startPos = new Vector2(startPos);
        this.segment = segment;
        this.distance = dist;
    }

    @Override
    public int compareTo(Object o) {
        Collision other = (Collision)o;
        return (int)(other.distance - this.distance);
    }
}
