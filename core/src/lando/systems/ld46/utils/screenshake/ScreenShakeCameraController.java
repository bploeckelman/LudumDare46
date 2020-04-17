package lando.systems.ld46.utils.screenshake;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

public class ScreenShakeCameraController {

    public float maxXOffset = 20;
    public float maxYOffset = 20;
    public float maxAngleDegrees = 1;

    public float xOffsetSpeed = 3f;
    public float yOffsetSpeed = 3f;
    public float rotationSpeed = 2f;


    private OrthographicCamera worldCamera;
    private OrthographicCamera viewCamera;
    private SimplexNoise noise;
    private float trauma;
    private float accumTime;
    private Texture debugTexture;
    private NinePatch outlineNinePatch;
    private Texture pixelTex;



    public ScreenShakeCameraController(OrthographicCamera worldCamera){
        this.worldCamera = worldCamera;
        viewCamera = new OrthographicCamera(worldCamera.viewportWidth, worldCamera.viewportHeight);
        noise = new SimplexNoise(16, .8f, 2);
        trauma = 0;
//        pixelTex = new Texture("white-pixel.png");
//        Texture outLineTexture = new Texture("outline.png");
//        outlineNinePatch = new NinePatch(outLineTexture, 4, 4, 4, 4);

//        Pixmap pixmap = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
//        for (int x = 0; x < 128; x++){
//            for (int y = 0; y < 128; y++){
//                float noiseValue = (float)((noise.getNoise(x, y) + 1f)/2f);
//                int color = Color.rgba8888(noiseValue, noiseValue, noiseValue, 1f);
//                pixmap.drawPixel(x, y, color);
//            }
//        }
//        debugTexture = new Texture(pixmap);
//        pixmap.dispose();
    }


    /**
     * Called every frame.
     * This will update the shake camera
     * @param dt frame delta
     */
    public void update(float dt){
        accumTime += dt;

        // reset view camera
        viewCamera.position.set(worldCamera.position);
        viewCamera.up.set(worldCamera.up);
        viewCamera.zoom = worldCamera.zoom;

        trauma = MathUtils.clamp(trauma, 0f, 1f);
        float shake = getShakeAmount();
        float offsetX = maxXOffset * shake * worldCamera.zoom * (float)noise.getNoise(1, accumTime * xOffsetSpeed);
        float offsetY = maxYOffset * shake * worldCamera.zoom * (float)noise.getNoise(20, accumTime * yOffsetSpeed);
        float angle = maxAngleDegrees * shake * (float)noise.getNoise(30, accumTime * rotationSpeed);

        viewCamera.position.add(offsetX, offsetY, 0);
        viewCamera.rotate(angle);
//        viewCamera.rotateAround(new Vector3(viewCamera.position), viewCamera.direction, angle);
        viewCamera.update();

        trauma = MathUtils.clamp(trauma - (dt/2f), 0f, 1f);

    }


    /**
     * Adds damage to the screen shake amount, values between .1 and .5 work best
     * Max combined damage trauma is 1f
     * @param damage between 0 and 1
     */
    public void addDamage(float damage){
        trauma += damage;
    }

    private float getShakeAmount(){
        return trauma * trauma;
    }


    /**
     * Use this instead of the normal cameras projection Matrix
     * @return the shaken camera matrix
     */
    public Matrix4 getCombinedMatrix(){
        return viewCamera.combined;
    }

    public OrthographicCamera getViewCamera() {
        return viewCamera;
    }

    public void renderDebug(SpriteBatch batch, OrthographicCamera screenCamera){
        batch.setColor(Color.WHITE);
        batch.draw(debugTexture, screenCamera.viewportWidth - 148, 20);
        float height = screenCamera.viewportHeight - 40;
        batch.setColor(Color.RED);
        batch.draw(pixelTex, 20, 20, 20, height * trauma);
        batch.draw(pixelTex, 45, 20, 20, height * getShakeAmount());

        batch.setColor(Color.WHITE);
        outlineNinePatch.draw(batch, 20, 20, 20, height);
        outlineNinePatch.draw(batch, 45, 20, 20, height);
    }
}
