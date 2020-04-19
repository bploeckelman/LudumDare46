package lando.systems.ld46.backgrounds;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class ParallaxBackground {

    /**
     * this array contains the parallax scrolling layers that are drawn on the screen. Layers are rendered in the order they are present in this array.
     */
    private final Array<ParallaxLayer> layers;
    private final Matrix4 cachedProjectionView;
    private final Vector3 cachedPos;
    private float cachedZoom;

    /**
     * Create a ParallaxBackground without any layers
     */
    public ParallaxBackground() {
        this.layers = new Array<>();
        this.cachedPos = new Vector3();
        this.cachedProjectionView = new Matrix4();
    }

    /**
     * Create a ParallaxBackground instance with the layers added
     * @param layers layers to be added to the parallaxBackground
     */
    public ParallaxBackground(ParallaxLayer... layers) {
        this();
        addLayers(layers);
    }

    /**
     * Add the layers to the {@link #layers} array. These layers are rendered over the layers previously in the layers array
     * @param layers layers to be added to the parallaxBackground
     */
    public void addLayers(ParallaxLayer... layers) {
        this.layers.addAll(layers);
    }

    /**
     * render the layers held by this module. Of course the layers are rendered in parallax scrolling manner. The worldCamera and batch provided are unaffected by the method
     * @param worldCamera The Orthographic WorldCamera , all layers are rendered relative to its position.
     * @param batch The batch which is used to render the layers.
     */
    public void render(OrthographicCamera worldCamera, Batch batch) {
        // stash the world camera props
        cachedProjectionView.set(worldCamera.combined);
        cachedPos.set(worldCamera.position);
        cachedZoom = worldCamera.zoom;

        for (int i = 0; i < layers.size; ++i) {
            ParallaxLayer layer = layers.get(i);

            // update world camera for this layer
            Vector2 origCameraPos = new Vector2(cachedPos.x, cachedPos.y);
            worldCamera.position.set(origCameraPos.scl(layer.parallaxRatio), cachedPos.z);
            worldCamera.update();
            batch.setProjectionMatrix(worldCamera.combined);

            float halfZoomedWidth  = worldCamera.viewportWidth  * worldCamera.zoom * 0.5f;
            float halfZoomedHeight = worldCamera.viewportHeight * worldCamera.zoom * 0.5f;

            // T_T
            float currentX = (layer.tileModeX == ParallaxLayer.TileMode.single) ? 0
                           : ((int) ((worldCamera.position.x - halfZoomedWidth) / layer.getWidth())) * layer.getWidth() - (Math.abs((1 - layer.parallaxRatio.x) % 1) * worldCamera.viewportWidth * 0.5f);
            do {
                // T_T
                float currentY = (layer.tileModeY == ParallaxLayer.TileMode.single) ? 0
                               : ((int) ((worldCamera.position.y - halfZoomedHeight) / layer.getHeight())) * layer.getHeight() - (((1 - layer.parallaxRatio.y) % 1) * worldCamera.viewportHeight * 0.5f);
                do {
                    if (!((worldCamera.position.x - halfZoomedWidth  > currentX + layer.getWidth())
                       || (worldCamera.position.x + halfZoomedWidth  < currentX)
                       || (worldCamera.position.y - halfZoomedHeight > currentY + layer.getHeight())
                       || (worldCamera.position.y + halfZoomedHeight < currentY))) {
                        layer.render(batch, currentX, currentY);
                    }
                    currentY += layer.getHeight();
                    if (layer.tileModeY == ParallaxLayer.TileMode.single) {
                        break;
                    }
                } while (currentY < worldCamera.position.y + halfZoomedHeight);

                currentX += layer.getWidth();
                if (layer.tileModeX == ParallaxLayer.TileMode.single) {
                    break;
                }
            } while (currentX < worldCamera.position.x + halfZoomedWidth);
        }

        // restore the old world camera props
        worldCamera.combined.set(cachedProjectionView);
        worldCamera.position.set(cachedPos);
        worldCamera.zoom = cachedZoom;
        worldCamera.update();
        batch.setProjectionMatrix(worldCamera.combined);
    }

}
