package lando.systems.ld46;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import lando.systems.ld46.accessors.*;

public class Game extends ApplicationAdapter {

	public Assets assets;
	public TweenManager tween;

	@Override
	public void create () {
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
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		assets.batch.begin();
		assets.batch.draw(assets.debugTexture, 0, 0);
		assets.batch.end();
	}
	
	@Override
	public void dispose () {
	    assets.dispose();
	}

}
