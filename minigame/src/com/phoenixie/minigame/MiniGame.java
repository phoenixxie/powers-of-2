package com.phoenixie.minigame;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

public class MiniGame extends Game {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture chiffreImage;
	private TextureRegion[] chiffres;
	private Pixmap cadrePixmap;
	private Texture cadre;
	private ShapeRenderer renderer;
	
	private int boiteCount = 5;
	
	static int CHIFFRE_MAX = 16;
	static int CHIFFRE_WIDTH = 128;
	static int CADRE_WIDTH = 8;
	static int LINE_WIDTH = 5;

	@Override
	public void create() {
		camera = new OrthographicCamera(1024, 1024);
		camera.position.set(1024 / 2, 1024 / 2, 0);

		batch = new SpriteBatch();
		renderer = new ShapeRenderer();
		
		chiffreImage = new Texture(Gdx.files.internal("data/chiffres.png"));
		chiffres = new TextureRegion[CHIFFRE_MAX];
		
		for (int i = 0; i < CHIFFRE_MAX; ++i) {
			chiffres[i] = new TextureRegion(chiffreImage,
					i * CHIFFRE_WIDTH, 0,
					CHIFFRE_WIDTH, CHIFFRE_WIDTH);
		}


	}
	
	private void drawCadre() {

		int boiteWidth = boiteCount * CHIFFRE_WIDTH + (boiteCount - 1) * LINE_WIDTH + 2 * CADRE_WIDTH;
		renderer.begin(ShapeType.FilledRectangle);
		renderer.setColor(1, 0, 0, 1);
		renderer.filledRect(0, 0, boiteWidth, CADRE_WIDTH);
		renderer.filledRect(0, 0, CADRE_WIDTH, boiteWidth);
		renderer.filledRect(boiteWidth - CADRE_WIDTH, 0, CADRE_WIDTH, boiteWidth);
		renderer.filledRect(0, boiteWidth - CADRE_WIDTH, boiteWidth, CADRE_WIDTH);
		renderer.end();
	}

	@Override
	public void dispose() {
		chiffreImage.dispose();
		batch.dispose();
	}

	@Override
	public void render() {		
		super.render();
		
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(chiffres[1], 100, 100);
		batch.end();
		
		renderer.setProjectionMatrix(camera.combined);
		drawCadre();
		
		if (Gdx.input.isTouched()) {

		}
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
