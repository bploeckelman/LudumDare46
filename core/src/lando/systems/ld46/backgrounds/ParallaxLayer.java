package lando.systems.ld46.backgrounds;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

public abstract class ParallaxLayer {

    /**
     * TileMode basically represents the way a layer has to repeat itself while scrolling. Parallax Scrolling can be performed infinitely in any direction if TileMode is set to <b>TileMode.repeat</b> in that direction.
     * The layer will just keep on repeating itself. Specifying <b>TileMode.single</b> makes the layer render only once without repeating in a direction
     * @author Rahul
     */
    public enum TileMode { repeat, single }

    protected Vector2 parallaxRatio = new Vector2();
    protected TileMode tileModeX = TileMode.repeat;
    protected TileMode tileModeY = TileMode.single;

    /**
     * returns the width of this layer. This width basically represents segment width of this layer after which it either repeats itself while rendering or just ceases to render further, depending upon the horizontal TileMode (see {@link #setTileModeX(TileMode)})
     * @return width of the layer
     */
    public abstract float getWidth();

    /**
     * returns the height of this layer. This height basically represents segment height of this layer after which it either repeats itself while rendering or just ceases to render further, depending upon the vertical TileMode (see {@link #setTileModeY(TileMode)})
     * @return returns the height of this layer
     */
    public abstract float getHeight();

    /**
     * draw this layer at specified position. Make sure that when you implement or extend this method you draw this layer within bounds returned by {@link #getWidth()} and {@link #getHeight()}.
     * @param batch the batch used for rendering
     * @param x the x position of the lower left corner where rendering should be done
     * @param y the y position of the lower left corner where rendering should be done
     */
    public abstract void render(Batch batch, float x, float y);

    /**
     * draw this layer at specified position. Make sure that when you implement or extend this method you draw this layer within bounds returned by {@link #getWidth()} and {@link #getHeight()}.
     * @param batch the batch used for rendering
     * @param pos the position of the lower left corner where rendering should be done
     */
    public void render(Batch batch, Vector2 pos) {
        this.render(batch, pos.x, pos.y);
    }

}
