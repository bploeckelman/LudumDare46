package lando.systems.ld46.physics;

import com.badlogic.gdx.math.Vector2;

public class Segment2D {
    public Vector2 start;
    public Vector2 end;
    public Vector2 delta;
    public Vector2 normal;


    public Segment2D() {
        this(new Vector2(0,0), new Vector2(1,0));
    }

    public Segment2D(Vector2 start, Vector2 end){
        this(start.x, start.y, end.x, end.y);
    }

    public Segment2D(float x1, float y1, float x2, float y2){
        this.start = new Vector2(x1, y1);
        this.end = new Vector2(x2, y2);
        this.delta = new Vector2(end).sub(start);
        this.normal = new Vector2(end).sub(start).nor().rotate90(-1);
    }

    public void fromSegment(Segment2D other){
        this.start.set(other.start);
        this.end.set(other.end);
        this.delta = new Vector2(end).sub(start);
        this.normal = new Vector2(end).sub(start).nor().rotate90(-1);
    }

    public float getRotation(){
        return delta.angle();
    }

    public void setEnd(Vector2 newEnd) {
        this.setEnd(newEnd.x, newEnd.y);
    }
    public void setEnd(float x, float y) {
        end.set(x, y);
        this.delta = new Vector2(end).sub(start);
        this.normal = new Vector2(end).sub(start).nor().rotate90(-1);
    }

    public void setStart(Vector2 newStart) {
        this.setStart(newStart.x, newStart.y);
    }

    public void setStart(float x, float y) {
        start.set(x, y);
        this.delta = new Vector2(end).sub(start);
        this.normal = new Vector2(end).sub(start).nor().rotate90(-1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Segment2D) {
            Segment2D other = (Segment2D) obj;
           return start.epsilonEquals(other.start) && end.epsilonEquals(other.end);
        } else
        return super.equals(obj);
    }
}
