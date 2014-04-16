package com.phoenixie.minigame;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ImageStore {
	public static final int IMGCHIFFRE_COUNT = 16;
	public static final int PHOTO_COUNT = 19;
	public static final int PIC_COUNT = 16;

	public static enum TextureType {
		CHIFFRES, PHOTOS, PICTURES
	}

	private static Random random = new Random(System.currentTimeMillis());

	private Texture[] imgChiffres;
	private Texture[] photos;
	private Texture[] pictures;
	private Texture[] currImages;
	private BitmapFont font;

	public ImageStore init(BitmapFont font) {
		imgChiffres = new Texture[IMGCHIFFRE_COUNT];
		for (int i = 0; i < IMGCHIFFRE_COUNT; ++i) {
			imgChiffres[i] = new Texture(Gdx.files.internal("data/chiffres/" + (i + 1) + ".jpg"));
		}

		photos = new Texture[PHOTO_COUNT];
		for (int i = 0; i < PHOTO_COUNT; ++i) {
			photos[i] = new Texture(Gdx.files.internal("data/camarades/" + (i + 1) + ".jpg"));
		}

		pictures = new Texture[PIC_COUNT];
		for (int i = 0; i < PIC_COUNT; ++i) {
			pictures[i] = new Texture(Gdx.files.internal("data/droles/" + (i + 1) + ".jpg"));
		}

		shuffle(photos);
		shuffle(pictures);

		currImages = imgChiffres;
		
		this.font = font;

		return this;
	}

	public ImageStore setTexture(TextureType texture) {
		if (texture == TextureType.CHIFFRES) {
			currImages = imgChiffres;
		} else if (texture == TextureType.PHOTOS) {
			currImages = photos;
		} else if (texture == TextureType.PICTURES) {
			currImages = pictures;
		}

		return this;
	}

	private void shuffle(Texture[] images) {

		for (int i = 0; i < images.length; i++) {
			int randomPosition = random.nextInt(images.length);
			Texture temp = images[i];
			images[i] = images[randomPosition];
			images[randomPosition] = temp;
		}
	}

	public void dispose() {
		for (int i = 0; i < IMGCHIFFRE_COUNT; ++i) {
			imgChiffres[i].dispose();
			imgChiffres[i] = null;
		}
		
		for (int i = 0; i < PHOTO_COUNT; ++i) {
			photos[i].dispose();
			photos[i] = null;
		}

		pictures = new Texture[PIC_COUNT];
		for (int i = 0; i < PIC_COUNT; ++i) {
			pictures[i].dispose();
			pictures[i] = null;
		}
	}
	
	public void reset() {
		shuffle(photos);
		shuffle(pictures);
	}
	
	public void draw(SpriteBatch batch, int chiffre, int x, int y, int width) {
		batch.draw(currImages[chiffre], x, y, width, width);
		if (currImages != imgChiffres) {
			font.draw(batch, "" + (chiffre + 1), x + 10, y + MiniGame.FONT_SIZE);
		}
	}
	
	public Texture getTexture(int num) {
		return currImages[num];
	}
}
