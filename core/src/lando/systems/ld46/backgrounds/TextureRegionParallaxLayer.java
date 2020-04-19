package lando.systems.ld46.backgrounds;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class TextureRegionParallaxLayer extends ParallaxLayer {

    private TextureRegion texRegion;
    private float regionWidth;
    private float regionHeight;

    public float padLeft = 0;
    public float padRight = 0;
    public float padBottom = 0;
    public float padTop = 0;

    /**
     * Creates a TextureRegionParallaxLayer with regionWidth and regionHeight equal that of the texRegion. Paddings are set to 0.
     * @param texRegion the texture region
     * @param parallaxScrollRatio the parallax ratio in x and y direction
     */
    public TextureRegionParallaxLayer(TextureRegion texRegion, Vector2 parallaxScrollRatio) {
        this.texRegion = texRegion;
        this.regionWidth = texRegion.getRegionWidth();
        this.regionHeight = texRegion.getRegionHeight();
        this.parallaxRatio.set(parallaxScrollRatio);
    }

    /**
     * Creates a TextureRegionParallaxLayer with regionWidth and regionHeight equal to parameters width and height. Paddings are set to 0.
     * @param texRegion the texture region
     * @param regionWidth width to be used as regionWidth
     * @param regionHeight height to be used as regionHeight
     * @param parallaxScrollRatio the parallax ratio in x and y direction
     */
    public TextureRegionParallaxLayer(TextureRegion texRegion, float regionWidth, float regionHeight, Vector2 parallaxScrollRatio) {
        this.texRegion = texRegion;
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.parallaxRatio.set(parallaxScrollRatio);
    }

    /**
     * Creates a TextureRegionParallaxLayer with either regionWidth or regionHeight equal oneDimen specified, while the other is calculated maintaining the aspect ratio of the region. Paddings are set to 0.
     * @param texRegion texRegion the texture region
     * @param oneDimen either regionWidth of regionHeight
     * @param parallaxScrollRatio the parallax ratio in x and y direction
     * @param wh what does oneDimen represent
     */
    public TextureRegionParallaxLayer(TextureRegion texRegion, float oneDimen, Vector2 parallaxScrollRatio, ParallaxUtils.WH wh) {
        this.texRegion = texRegion;
        switch (wh) {
            case width:
                this.regionWidth = oneDimen;
                this.regionHeight = ParallaxUtils.calculateOtherDimension(ParallaxUtils.WH.width, oneDimen, this.texRegion);
                break;
            case height:
                this.regionWidth = ParallaxUtils.calculateOtherDimension(ParallaxUtils.WH.height, oneDimen, this.texRegion);
                this.regionHeight = oneDimen;
                break;
        }
        this.parallaxRatio.set(parallaxScrollRatio);
    }

    /**
     * draws the texture region at x y ,with left and bottom padding
     * <p>
     * You might be wondering that why are topPadding and rightPadding not used , what is their use then . Well they are used by ParallaxBackground when it renders this layer . During rendering it pings the {@link #getWidth()}/{@link #getHeight()} method of this layer which in {@link TextureRegionParallaxLayer} implementation return the sum of regionWidth/regionHeight and paddings.
     */
    @Override
    public void render(Batch batch, float x, float y) {
        batch.draw(texRegion, x + padLeft, y + padBottom, regionWidth, regionHeight);
    }

    /**
     * returns the width of this layer (regionWidth+padLeft+padRight)
     */
    @Override
    public float getWidth() {
        return padLeft + regionWidth + padRight;
    }

    /**
     * returns the height of this layer (regionHeight+padTop+padBottom)
     */
    @Override
    public float getHeight() {
        return padTop + regionHeight + padBottom;
    }

    /**
     * sets left right top bottom padding to same value
     * @param pad padding
     */
    public void setAllPad(float pad){
        padLeft = pad;
        padRight = pad;
        padTop = pad;
        padBottom = pad;
    }

}
