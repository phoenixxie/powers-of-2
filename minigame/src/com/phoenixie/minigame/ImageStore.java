package com.phoenixie.minigame;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ImageStore {
	public static final int IMGCHIFFRE_MAX = 16;
	public static final int PHOTO_COUNT = 19;
	public static final int PIC_COUNT = 16;
	public static final int IMG_WIDTH = 128;

	public static enum TextureType {
		CHIFFRES, PHOTOS, PICTURES
	}

	private static Random random = new Random(System.currentTimeMillis());

	private Texture chiffreImage;
	private TextureRegion[] imgChiffres;
	private Texture photoImage;
	private TextureRegion[] photos;
	private Texture pictureImage;
	private TextureRegion[] pictures;
	private TextureRegion[] currImages;
	private BitmapFont font;

	public ImageStore init(BitmapFont font) {
		chiffreImage = new Texture(Gdx.files.internal("data/chiffres.png"));
		imgChiffres = new TextureRegion[IMGCHIFFRE_MAX];

		for (int i = 0; i < IMGCHIFFRE_MAX; ++i) {
			imgChiffres[i] = new TextureRegion(chiffreImage, i * IMG_WIDTH, 0,
					IMG_WIDTH, IMG_WIDTH);
		}

		photoImage = new Texture(Gdx.files.internal("data/photos.png"));
		photos = new TextureRegion[PHOTO_COUNT];

		for (int i = 0; i < PHOTO_COUNT; ++i) {
			photos[i] = new TextureRegion(photoImage, i * IMG_WIDTH, 0,
					IMG_WIDTH, IMG_WIDTH);
		}

		pictureImage = new Texture(Gdx.files.internal("data/pics.png"));
		pictures = new TextureRegion[IMGCHIFFRE_MAX];

		for (int i = 0; i < PIC_COUNT; ++i) {
			pictures[i] = new TextureRegion(pictureImage, i * IMG_WIDTH, 0,
					IMG_WIDTH, IMG_WIDTH);
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

	private void shuffle(TextureRegion[] images) {

		for (int i = 0; i < images.length; i++) {
			int randomPosition = random.nextInt(images.length);
			TextureRegion temp = images[i];
			images[i] = images[randomPosition];
			images[randomPosition] = temp;
		}
	}

	public void dispose() {
		chiffreImage.dispose();
		photoImage.dispose();
		pictureImage.dispose();
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
}
