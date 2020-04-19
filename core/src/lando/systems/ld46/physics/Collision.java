package lando.systems.ld46.physics;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Collision implements Comparable {
    public Intersector.MinimumTranslationVector distance;
    public Polygon polygon;
    public Segment2D segment;
    public Rectangle rect;
    public float t;

    public Collision() {}

    public Collision(Segment2D segment, Intersector.MinimumTranslationVector dist, Polygon polygon) {
        this.segment = segment;
        this.distance = dist;
        this.polygon = polygon;
    }


    @Override
    public int compareTo(Object o) {
        Collision other = (Collision)o;
        if (other.t < this.t) {
            return 1;
        } else if (other.t > this.t) {
            return -1;
        } else if (Math.abs(other.distance.normal.dot(other.segment.normal)) < Math.abs(this.distance.normal.dot(this.segment.normal))) {
            return 1;
        } else {
            return -1;
        }
//        return (int)(Math.abs(other.distance.normal.dot(other.segment.normal)) - Math.abs(this.distance.normal.dot(this.segment.normal)));
    }
}
