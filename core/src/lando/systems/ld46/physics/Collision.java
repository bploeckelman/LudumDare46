package lando.systems.ld46.physics;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Collision implements Comparable, Pool.Poolable {
    public Intersector.MinimumTranslationVector distance;
    public Polygon polygon;
    public Segment2D segment;
    public Rectangle rect;
    public Vector2 velocity;
    public float t;
    public float dotProduct;

    public Collision() {
        polygon = new Polygon();
        rect = new Rectangle();
        velocity = new Vector2();
    }

    public Collision(Segment2D segment, Intersector.MinimumTranslationVector dist, Polygon polygon) {
        this.segment = segment;
        this.distance = dist;
        this.polygon = polygon;
    }

    public void init(Segment2D segment, Intersector.MinimumTranslationVector dist){
        this.segment = segment;
        this.distance = dist;
        this.dotProduct = Math.abs(this.distance.normal.dot(this.segment.normal));
    }


    @Override
    public int compareTo(Object o) {
        Collision other = (Collision)o;
        if (other.distance.depth < this.distance.depth) {
            return 1;
        } else if (other.distance.depth > this.distance.depth) {
            return -1;
        } else if (other.dotProduct > this.dotProduct) {
            return 1;
        } else {
            return -1;
        }
//        return (int)(Math.abs(other.distance.normal.dot(other.segment.normal)) - Math.abs(this.distance.normal.dot(this.segment.normal)));
    }

    @Override
    public void reset() {

    }
}
