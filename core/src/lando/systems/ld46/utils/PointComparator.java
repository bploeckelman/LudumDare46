package lando.systems.ld46.utils;

import com.badlogic.gdx.math.Vector2;

import java.util.Comparator;

public class PointComparator implements Comparator<Vector2> {

    public Vector2 center;

    @Override
    public int compare(Vector2 a, Vector2 b) {
        double a1 = (Math.toDegrees(Math.atan2(a.x - center.x, a.y - center.y)) + 360) % 360;
        double a2 = (Math.toDegrees(Math.atan2(b.x - center.x, b.y - center.y)) + 360) % 360;
        return (int) (a1 - a2);
    }
}
