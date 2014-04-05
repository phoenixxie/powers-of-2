package com.phoenixie.minigame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "minigame";
		cfg.useGL20 = false;
		cfg.width = 800;
		cfg.height = 1280;
		
		new LwjglApplication(new MiniGame(), cfg);
	}
}
