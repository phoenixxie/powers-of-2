package com.phoenixie.minigame;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class MiniGame implements ApplicationListener {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture bucketImage;
	private Sprite sprite;
	private Rectangle bucket;

	@Override
	public void create() {
		camera = new OrthographicCamera(512, 512);
		camera.position.set(512 / 2, 512 / 2, 0);

		batch = new SpriteBatch();
		
		bucketImage = new Texture(Gdx.files.internal("data/libgdx.png"));
		
		bucket = new Rectangle();
		bucket.x = 0;
		bucket.y = 0;
		bucket.width = 512;
		bucket.height = 512;
	}

	@Override
	public void dispose() {
		bucketImage.dispose();
		batch.dispose();
	}

	@Override
	public void render() {		
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		batch.end();
		
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
