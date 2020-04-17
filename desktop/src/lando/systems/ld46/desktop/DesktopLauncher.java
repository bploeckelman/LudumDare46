package lando.systems.ld46.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import lando.systems.ld46.Config;
import lando.systems.ld46.Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = Config.title;
		config.width = Config.windowWidth;
		config.height = Config.windowHeight;
		config.resizable = Config.resizable;
		config.fullscreen = Config.fullscreen;
		config.vSyncEnabled = Config.vsync;
		config.forceExit = false;
		new LwjglApplication(new Game(), config); }
}
