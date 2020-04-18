package lando.systems.ld46;

import aurelienribon.tweenengine.*;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import lando.systems.ld46.accessors.*;
import lando.systems.ld46.screens.BaseScreen;
import lando.systems.ld46.screens.LaunchScreen;
import lando.systems.ld46.screens.TitleScreen;

public class Game extends ApplicationAdapter {

	public static Game game;

	public Pool<Vector2> vector2Pool = Pools.get(Vector2.class);
	public Pool<Color>   colorPool   = Pools.get(Color.class);

//	public AudioManager audio;
	public Assets assets;
	public TweenManager tween;
	public Audio audio;

	private BaseScreen currentScreen;
	private BaseScreen nextScreen;
	private MutableFloat transitionPercent;
	private FrameBuffer transitionFBO;
	private FrameBuffer originalFBO;
	Texture originalTexture;
	Texture transitionTexture;
	ShaderProgram transitionShader;
	boolean transitioning;

	public Game() { game = this; }

	@Override
	public void create () {
		transitionPercent = new MutableFloat(0);
		transitionFBO = new FrameBuffer(Pixmap.Format.RGBA8888, Config.windowWidth, Config.windowHeight, false);
		transitionTexture = transitionFBO.getColorBufferTexture();


		originalFBO = new FrameBuffer(Pixmap.Format.RGBA8888, Config.windowWidth, Config.windowHeight, false);
		originalTexture = originalFBO.getColorBufferTexture();

		transitioning = false;

		if (tween == null) {
			tween = new TweenManager();
			Tween.setWaypointsLimit(4);
			Tween.setCombinedAttributesLimit(4);
			Tween.registerAccessor(Color.class, new ColorAccessor());
			Tween.registerAccessor(Rectangle.class, new RectangleAccessor());
			Tween.registerAccessor(Vector2.class, new Vector2Accessor());
			Tween.registerAccessor(Vector3.class, new Vector3Accessor());
			Tween.registerAccessor(OrthographicCamera.class, new CameraAccessor());
		}

		if (assets == null) {
			assets = new Assets();
		}

		if (audio == null) {
			audio = new Audio(true, this);
		}

		setScreen(new LaunchScreen(this));
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float dt = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);

		audio.update(dt);
		tween.update(dt);
		currentScreen.update(dt);
		currentScreen.renderFrameBuffers(assets.batch);

		if (nextScreen != null) {
			nextScreen.update(dt);
			nextScreen.renderFrameBuffers(assets.batch);
			transitionFBO.begin();
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			nextScreen.render(assets.batch);
			transitionFBO.end();

			originalFBO.begin();
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			currentScreen.render(assets.batch);
			originalFBO.end();

			assets.batch.setShader(transitionShader);
			assets.batch.begin();
			originalTexture.bind(1);
			transitionShader.setUniformi("u_texture1", 1);
			transitionTexture.bind(0);
			transitionShader.setUniformf("u_percent", transitionPercent.floatValue());
			assets.batch.setColor(Color.WHITE);
			assets.batch.draw(transitionTexture, 0,0, Config.windowWidth, Config.windowHeight);
			assets.batch.end();
			assets.batch.setShader(null);
		} else {
			currentScreen.render(assets.batch);
		}
	}
	
	@Override
	public void dispose () {
	    assets.dispose();
	}


	public void setScreen(BaseScreen screen) {
		setScreen(screen, null, 1f);
	}

	public void setScreen(final BaseScreen newScreen, ShaderProgram transitionType, float transitionSpeed) {
		if (nextScreen != null) return;
		if (transitioning) return; // only want one transition
		if (currentScreen == null) {
			currentScreen = newScreen;
		} else {
			transitioning = true;
			if (transitionType == null) {
				transitionShader = assets.randomTransitions.get(MathUtils.random(assets.randomTransitions.size - 1));
			} else {
				transitionShader = transitionType;
			}
			transitionPercent.setValue(0);
			Timeline.createSequence()
					.pushPause(.1f)
					.push(Tween.call((i, baseTween) -> nextScreen = newScreen))
					.push(Tween.to(transitionPercent, 1, transitionSpeed)
							.target(1))
					.push(Tween.call((i, baseTween) -> {
						currentScreen = nextScreen;
						nextScreen = null;
						transitioning = false;
					}))
					.start(tween);
		}
	}

	public BaseScreen getScreen() {
		return currentScreen;
	}
}
