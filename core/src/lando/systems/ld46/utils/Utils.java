package lando.systems.ld46.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import lando.systems.ld46.Assets;
import lando.systems.ld46.physics.Segment2D;

public class Utils {

    public static Color hsvToRgb(float hue, float saturation, float value, Color outColor) {
        if (outColor == null) outColor = new Color();
        hue = hue % 1f;
        int h = (int) (hue * 6);
        h = h % 6;
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        switch (h) {
            case 0: outColor.set(value, t, p, 1f); break;
            case 1: outColor.set(q, value, p, 1f); break;
            case 2: outColor.set(p, value, t, 1f); break;
            case 3: outColor.set(p, q, value, 1f); break;
            case 4: outColor.set(t, p, value, 1f); break;
            case 5: outColor.set(value, p, q, 1f); break;
            default: throw new GdxRuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
        }
        return outColor;
    }

    public static Vector2 reflectVector(Vector2 incoming, Vector2 normal) {
        float initalSize = incoming.len();
        normal.nor();
        incoming.nor();
        float iDotN = incoming.dot(normal);
        incoming.set(incoming.x - 2f * normal.x * iDotN,
                incoming.y - 2f * normal.y * iDotN)
                .nor().scl(initalSize);
        return incoming;
    }

    public static void drawSegment(Assets assets, Segment2D segment, float width, Color c) {
        assets.batch.setColor(c);
        assets.batch.draw(assets.whitePixel, segment.start.x, segment.start.y - width/2f, 0, width/2f, segment.delta.len(), width, 1, 1, segment.getRotation());
        assets.batch.draw(assets.whitePixel, segment.start.x + segment.delta.x/2, segment.start.y + segment.delta.y/2, 0,0, 10, 1, 1, 1, segment.normal.angle());
        assets.batch.setColor(Color.WHITE);
    }
}
