package com.phoenixie.minigame;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.phoenixie.minigame.utils.Queue;

public class MiniGame extends Game implements GestureListener {
	static final int FONT_SIZE = 48;

	static final int SCREEN_WIDTH = 800;
	static final int SCREEN_HEIGHT = 1280;
	static final float ASPECT_RATIO = (float) SCREEN_WIDTH
			/ (float) SCREEN_HEIGHT;
	
	static final int BUTTON_UNDO = 0;
	static final int BUTTON_RESET = 1;
	static final int BUTTON_RESUME = 2;
	static final int BUTTON_SETTINGS = 3;

	static final int BUTTON_WIDTH = 240;
	static final int BUTTON_HEIGHT = 80;
	static final int BOTTOM_SPACE = 150;
	
	static final int ANIME_SPEED = 10;

	static final Color COLEUR_BACKGROUND = new Color(0.96875f, 0.9453125f,
			0.8515625f, 1);
	static final Color COLEUR_CADRE = new Color(0.77734375f, 0.68359375f,
			0.73828125f, 1);

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Stage stage;
	private Skin skin;
	private BitmapFont font;
	private Kryo kryo;
	
	private ShapeRenderer renderer;
	private Rectangle viewport;

	private Texture buttonImage;
	private ImageButton buttonReset;
	private ImageButton buttonUndo;
	private ImageButton buttonSettings;

	private CheckBox checkChiffres;
	private CheckBox checkPhotos;
	private CheckBox checkPictures;
	private CheckBox checkTable44;
	private CheckBox checkTable55;
	private CheckBox checkTable66;
	private Dialog dialogSettings;
	private Dialog dialogReset;
	private Dialog dialog2048;
	private Dialog dialog4096;
	private Dialog dialog8192;
	private Dialog dialog16384;
	private Dialog dialog32768;
	private Dialog dialog65536;

	enum Direction {
		HOLD, UP, DOWN, LEFT, RIGHT
	}

	static class Pos {
		public int x;
		public int y;
	}

	private static class GameState {
		public int tableSize;
		public int gameCount;
		public long gameScore;
		public long gameTime;
		public int[][][] gameEtapes;
		public int[][] chiffreTable;
	}

	private static class Settings {
		public int boiteType = 0;
		public int tableSize = 4;
	};

	private Pos tableVertex;
	private Pos tableLeftBottom;
	private Pos tableRightTop;

	private Queue etapeQueue;
	private int gameCount;
	private long gameScore;
	private long gameTime;
	private long gameStartTime;
	private int[][][] gameEtapeBuffer;
	private GameState gameState = new GameState();
	private boolean gameLoaded = false;
	private Settings settings = new Settings();

	private Grille grille = new Grille();
	private ImageStore imageStore = new ImageStore();

	@Override
	public void create() {
		// Gdx.graphics.setContinuousRendering(false);
		Gdx.graphics.requestRendering();
		Texture.setEnforcePotImages(false);

		stage = new Stage();
		stage.setViewport(SCREEN_WIDTH, SCREEN_HEIGHT, true);

		etapeQueue = new Queue();
		gameEtapeBuffer = new int[etapeQueue.capacity()][][];
		kryo = new Kryo();

		camera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
		camera.position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
		stage.setCamera(camera);

		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		font = new BitmapFont(Gdx.files.internal("data/fonts/fonts.fnt"),
				Gdx.files.internal("data/fonts/fonts.png"), false);
		renderer = new ShapeRenderer();
		
		tableVertex = new Pos();
		tableVertex.x = (SCREEN_WIDTH - Grille.TABLE_WIDTH) / 2;
		tableVertex.y = BOTTOM_SPACE;
		tableRightTop = new Pos();
		
		tableLeftBottom = tableVertex;
		tableRightTop.x = tableVertex.x + Grille.TABLE_WIDTH;
		tableRightTop.y = tableVertex.y + Grille.TABLE_WIDTH;
		
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, 
				new GestureDetector(0.0f, 0.0f, 0.0f, 5f, this) {
					private boolean wrongStart = false;

					@Override
					public boolean touchDown(int x, int y, int pointer,
							int button) {
						Vector3 touchPos = new Vector3(x, y, 0);
						camera.unproject(touchPos);

						if (touchPos.x < tableLeftBottom.x
								|| touchPos.x > tableRightTop.x) {
							wrongStart = true;
							return false;
						}
						if (touchPos.y < tableLeftBottom.y
								|| touchPos.y > tableRightTop.y) {
							wrongStart = true;
							return false;
						}

						wrongStart = false;
						return super.touchDown((float) x, (float) y, pointer,
								button);
					}

					@Override
					public boolean touchDragged(int x, int y, int pointer) {
						if (wrongStart) {
							return false;
						}
						return super
								.touchDragged((float) x, (float) y, pointer);
					}

					@Override
					public boolean touchUp(int x, int y, int pointer, int button) {
						if (wrongStart) {
							return false;
						}
						return super.touchUp((float) x, (float) y, pointer,
								button);
					}
				}));

		loadSettings();
		
//		if (settings.boiteType == BoiteType.CHIFFRES) {
//			imageStore.setTexture(ImageStore.TextureType.CHIFFRES);
//		} else if (settings.boiteType == BoiteType.PHOTOS) {
//			imageStore.setTexture(ImageStore.TextureType.PHOTOS);
//		} else if (settings.boiteType == BoiteType.PICTURES) {
//			imageStore.setTexture(ImageStore.TextureType.PICTURES);
//		}
		
		grille.create(tableVertex);
		grille.setTableSize(settings.tableSize);
		
		loadGame();

		drawButtons();

		createDialogs();

		if (gameLoaded) {
			resumeGame();
		} else {
			resetGame();
		}

	}
	

	private void saveSettings() {
		if (!Gdx.files.isLocalStorageAvailable()) {
			return;
		}

		FileHandle file = Gdx.files.local("settings");
		OutputStream ostream = file.write(false);
		Output output = new Output(ostream);

		kryo.writeObject(output, settings);
		output.close();
	}
	
	private void loadSettings() {
		if (!Gdx.files.isLocalStorageAvailable()) {
			return;
		}

		try {
			FileHandle file = Gdx.files.local("settings");
			InputStream istream = file.read();
			Input input = new Input(istream);
			settings = kryo.readObject(input, Settings.class);
			input.close();
		} catch (Exception e) {
//			e.printStackTrace();
			return;
		}
	}
	
	private void saveGame() {
		if (!Gdx.files.isLocalStorageAvailable()) {
			return;
		}

		int etapeCount = etapeQueue.dump(gameEtapeBuffer);

		FileHandle file = Gdx.files.local("gamedata");
		OutputStream ostream = file.write(false);
		Output output = new Output(ostream);

		gameState.tableSize = grille.getTableSize();
		gameState.gameCount = gameCount;
		gameState.gameScore = gameScore;
		gameState.gameTime = gameTime;
//		gameState.chiffreTable = chiffreTable;
		gameState.gameEtapes = gameEtapeBuffer;

		kryo.writeObject(output, gameState);
		output.close();
	}

	private boolean loadGame() {
		gameLoaded = false;
		if (!Gdx.files.isLocalStorageAvailable()) {
			return false;
		}

		try {
			FileHandle file = Gdx.files.local("gamedata");
			InputStream istream = file.read();
			Input input = new Input(istream);
			gameState = kryo.readObject(input, GameState.class);
			input.close();

			if (gameState.gameCount == 0) {
				return false;
			}

			gameLoaded = true;
		} catch (Exception e) {
//			e.printStackTrace();
			return false;
		}

		return true;
	}

	private void resumeGame() {
//		if (boiteHorzCount != gameState.tableSize) {
//			setTableSize(gameState.tableSize);
//		}
//		
//		resetGame();
//
//		gameCount = gameState.gameCount;
//		gameScore = gameState.gameScore;
//		gameTime = gameState.gameTime;
//		chiffreTable = gameState.chiffreTable;
//		
//		for (int i = 0; i < boiteHorzCount; ++i) {
//			for (int j = 0; j < boiteHorzCount; ++j) {
//				int cur = chiffreTable[i][j];
//				if (maxChiffre < cur) {
//					maxChiffre = cur;
//				}
//			}
//		}
//
//		for (int i = 0; i < gameState.gameEtapes.length; ++i) {
//			if (gameState.gameEtapes[i] == null) {
//				break;
//			}
//			etapeQueue.enqueue(gameState.gameEtapes[i]);
//		}
//
//		if (!etapeQueue.isEmpty()) {
//			buttonUndo.setVisible(true);
//			buttonReset.setVisible(true);
//		}
//
//		Gdx.graphics.requestRendering();

	}

	private void resetGame() {
		gameCount = 0;
		gameScore = 0;
		gameTime = 0;
		gameStartTime = System.currentTimeMillis();

		etapeQueue.clear();
		buttonUndo.setVisible(false);
		buttonReset.setVisible(false);

		grille.reset();
	}

	private void drawStat() {
		long currTime = System.currentTimeMillis();
		long delta = currTime - gameStartTime;

		gameTime += delta;
		gameStartTime = currTime;

		currTime = gameTime / 1000;

		int seconds = (int) (currTime % 60);
		currTime /= 60;
		int minutes = (int) (currTime % 60);
		int hours = (int) (currTime / 60);

		font.setColor(0f, 0f, 0f, 1f);
		font.draw(batch, "Points:   " + gameScore, 20, SCREEN_HEIGHT
				- FONT_SIZE - 30);
		font.draw(batch, "Temps:   " + hours + ":"
				+ (minutes < 10 ? "0" + minutes : minutes) + ":"
				+ (seconds < 10 ? "0" + seconds : seconds), 20, SCREEN_HEIGHT
				- FONT_SIZE - 100);
		font.draw(batch, "Tours:   " + gameCount, SCREEN_WIDTH / 2,
				SCREEN_HEIGHT - FONT_SIZE - 100);

	}

	private void drawButtons() {
		buttonImage = new Texture(Gdx.files.internal("data/buttons.png"));
		TextureRegion buttonUp = new TextureRegion(buttonImage, BUTTON_UNDO
				* BUTTON_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
		TextureRegion buttonDown = new TextureRegion(buttonImage, BUTTON_UNDO
				* BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT);

		ImageButtonStyle style = new ImageButtonStyle();
		style.imageUp = new TextureRegionDrawable(buttonUp);
		style.imageDown = new TextureRegionDrawable(buttonDown);
		buttonUndo = new ImageButton(style);

		buttonUp = new TextureRegion(buttonImage, BUTTON_RESET * BUTTON_WIDTH,
				0, BUTTON_WIDTH, BUTTON_HEIGHT);
		buttonDown = new TextureRegion(buttonImage,
				BUTTON_RESET * BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_WIDTH,
				BUTTON_HEIGHT);

		style = new ImageButtonStyle();
		style.imageUp = new TextureRegionDrawable(buttonUp);
		style.imageDown = new TextureRegionDrawable(buttonDown);
		buttonReset = new ImageButton(style);

		buttonUp = new TextureRegion(buttonImage, BUTTON_SETTINGS
				* BUTTON_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
		buttonDown = new TextureRegion(buttonImage, BUTTON_SETTINGS
				* BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT);

		style = new ImageButtonStyle();
		style.imageUp = new TextureRegionDrawable(buttonUp);
		style.imageDown = new TextureRegionDrawable(buttonDown);
		buttonSettings = new ImageButton(style);

		buttonUndo.setX(SCREEN_WIDTH - 20 - BUTTON_WIDTH);
		buttonUndo.setY(BOTTOM_SPACE + Grille.TABLE_WIDTH + 40);
		buttonReset.setX(20);
		buttonReset.setY(BOTTOM_SPACE + Grille.TABLE_WIDTH + 40);
		buttonSettings.setX(SCREEN_WIDTH - 20 - BUTTON_WIDTH);
		buttonSettings.setY(SCREEN_HEIGHT - 30 - BUTTON_HEIGHT);

		stage.addActor(buttonUndo);
		stage.addActor(buttonReset);
		stage.addActor(buttonSettings);

		buttonReset.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				dialogReset.show(stage);
			}

		});

		buttonUndo.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int[][] load = (int[][]) etapeQueue.pop();
				if (load != null) {
//					chiffreTable = load;
					Gdx.graphics.requestRendering();
					if (etapeQueue.isEmpty()) {
						buttonUndo.setVisible(false);
					}
				}
			}
		});

		buttonSettings.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				showSettings();
			}

		});
	}

	private void createDialogs() {
		checkChiffres = new CheckBox("Chiffres", skin);
		checkPhotos = new CheckBox("Photos", skin);
		checkPictures = new CheckBox("Images", skin);
		
		checkChiffres.getCells().get(0).size(30, 30);
		checkPhotos.getCells().get(0).size(30, 30);
		checkPictures.getCells().get(0).size(30, 30);

		ButtonGroup buttonTable = new ButtonGroup(checkChiffres,
				checkPhotos, checkPictures);
		buttonTable.setMaxCheckCount(1);
		buttonTable.setMinCheckCount(1);
		buttonTable.setUncheckLast(true);
		
		checkTable44 = new CheckBox("4 X 4", skin);
		checkTable55 = new CheckBox("5 X 5", skin);
		checkTable66 = new CheckBox("6 X 6", skin);
		
		checkTable44.getCells().get(0).size(30, 30);
		checkTable55.getCells().get(0).size(30, 30);
		checkTable66.getCells().get(0).size(30, 30);
		
		ButtonGroup tableSize = new ButtonGroup(checkTable44, checkTable55, checkTable66);
		tableSize.setMaxCheckCount(1);
		tableSize.setMinCheckCount(1);
		tableSize.setUncheckLast(true);

		Dialog.fadeDuration = 0.2f;
		dialogSettings = new Dialog("Paramètres", skin, "dialog") {
			protected void result(Object object) {
				if (!(Boolean) object) {
					return;
				}

				if (checkChiffres.isChecked()) {
					imageStore.setTexture(ImageStore.TextureType.CHIFFRES);
				} else if (checkPhotos.isChecked()) {
					imageStore.setTexture(ImageStore.TextureType.PHOTOS);
				} else if (checkPictures.isChecked()) {
					imageStore.setTexture(ImageStore.TextureType.PICTURES);
				}
				
				int tableSize = settings.tableSize;
				if (checkTable44.isChecked()) {
					tableSize = 4;
				} else if (checkTable55.isChecked()) {
					tableSize = 5;
				} else if (checkTable66.isChecked()) {
					tableSize = 6;
				}
				
				if (tableSize != settings.tableSize) {
					settings.tableSize = settings.tableSize = tableSize;
					buttonReset.setVisible(true);
				}
				
				saveSettings();
			}
		};

		dialogSettings.center();
		dialogSettings.padTop(100).padBottom(30);

		Table table = dialogSettings.getContentTable();
		table.defaults().width(250);
		table.add(checkChiffres);
		table.add(checkPhotos);
		table.add(checkPictures);
		table.row().padTop(20);
		table.add(checkTable44);
		table.add(checkTable55);
		table.add(checkTable66);
		table.row();

		table = dialogSettings.getButtonTable();
		table.padTop(30);
		TextButton dbutton = new TextButton("OK", skin);
		table.add(dbutton).width(200f);
		dialogSettings.setObject(dbutton, true);
		dbutton = new TextButton("Annuler", skin);
		table.add(dbutton).width(200f);
		dialogSettings.setObject(dbutton, false);

		dialogSettings.invalidateHierarchy();
		dialogSettings.invalidate();
		dialogSettings.layout();
		
		dialogReset = new Dialog("", skin, "dialog") {
			protected void result (Object object) {
				Boolean v = (Boolean)object;
				
				if (v) {
					if (grille.getTableSize() != settings.tableSize) {
						grille.setTableSize(settings.tableSize);
					}
					resetGame();
				}
			}
		}.text("Êtes-vous sûr de relancer le jeu?").button("Oui!", true).button("Non!", false).key(Keys.ENTER, true)
			.key(Keys.ESCAPE, false);
		
		dialog2048 = new Dialog("2048!", skin, "dialog").text("Très bien! Continuez!").button("OK", true);
		
		dialog4096 = new Dialog("4096!!", skin, "dialog").text("Excellent! Continuez!").button("OK", true);
		
		dialog8192 = new Dialog("8192!!", skin, "dialog").text("Intelligent! Continuez!").button("OK", true);
		
		dialog16384 = new Dialog("16384!!", skin, "dialog").text("Génial!! Continuez!").button("OK", true);
		
		dialog32768 = new Dialog("32768!!", skin, "dialog").text("Incroyable! Continuez!").button("OK", true);
		
		dialog65536 = new Dialog("65536!!", skin, "dialog")
			.text("Félicitation!\nVous avez obtenu le maximal point!!!").button("OK", true);
	}

	private void showSettings() {
//		if (settings.boiteType == BoiteType.CHIFFRES) {
//			checkChiffres.setChecked(true);
//		} else if (settings.boiteType == BoiteType.PHOTOS) {
//			checkPhotos.setChecked(true);
//		} else {
//			checkPictures.setChecked(true);
//		}
		
		switch (settings.tableSize) {
		case 4: checkTable44.setChecked(true); break;
		case 5: checkTable55.setChecked(true); break;
		case 6: checkTable66.setChecked(true); break;
		}

		dialogSettings.show(stage);
	}

	@Override
	public void dispose() {
		batch.dispose();
		renderer.dispose();
	}

	@Override
	public void render() {
		super.render();

		camera.update();
		camera.apply(Gdx.gl10);
		Gdx.gl.glViewport((int) viewport.x, (int) viewport.y,
				(int) viewport.width, (int) viewport.height);

		Gdx.gl.glClearColor(COLEUR_BACKGROUND.r, COLEUR_BACKGROUND.g,
				COLEUR_BACKGROUND.b, COLEUR_BACKGROUND.a);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);
		renderer.setProjectionMatrix(camera.combined);
		
		grille.draw(renderer, batch);
		
		batch.begin();
		drawStat();
		batch.end();

		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f));
		stage.draw();

		if (Gdx.input.isKeyPressed(Keys.LEFT))
			move(Direction.LEFT);
		else if (Gdx.input.isKeyPressed(Keys.RIGHT))
			move(Direction.RIGHT);
		else if (Gdx.input.isKeyPressed(Keys.UP))
			move(Direction.UP);
		else if (Gdx.input.isKeyPressed(Keys.DOWN))
			move(Direction.DOWN);
	}

	@Override
	public void resize(int width, int height) {
		float aspectRatio = (float) width / (float) height;
		float scale = 1f;
		Vector2 crop = new Vector2(0f, 0f);

		if (aspectRatio > ASPECT_RATIO) {
			scale = (float) height / (float) SCREEN_HEIGHT;
			crop.x = (width - SCREEN_WIDTH * scale) / 2f;
		} else if (aspectRatio < ASPECT_RATIO) {
			scale = (float) width / (float) SCREEN_WIDTH;
			crop.y = (height - SCREEN_HEIGHT * scale) / 2f;
		} else {
			scale = (float) width / (float) SCREEN_WIDTH;
		}

		float w = (float) SCREEN_WIDTH * scale;
		float h = (float) SCREEN_HEIGHT * scale;
		viewport = new Rectangle(crop.x, crop.y, w, h);
		
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
		gameStartTime = System.currentTimeMillis();
	}
	
	private void felicitation(int cur) {
		
//		if (cur > maxChiffre) {
//			switch (cur) {
//			case 10: dialog2048.show(stage); break;
//			case 11: dialog4096.show(stage); break;
//			case 12: dialog8192.show(stage); break;
//			case 13: dialog16384.show(stage); break;
//			case 14: dialog32768.show(stage); break;
//			case 15: dialog65536.show(stage); break;
//			}
//			maxChiffre = cur;
//		}
	}
	
	private void move(Direction direction) {
		grille.move(direction);
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		float x = Math.abs(velocityX);
		float y = Math.abs(velocityY);
		int comp = Float.compare(x, y);

		if (comp > 0) {
			if (velocityX > 0.0) {
				move(Direction.RIGHT);
			} else if (velocityX < 0.0) {
				move(Direction.LEFT);
			} else {
				return false;
			}
		} else {
			if (velocityY > 0.0) {
				move(Direction.DOWN);
			} else if (velocityY < 0.0) {
				move(Direction.UP);
			} else {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		return false;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
			Vector2 pointer1, Vector2 pointer2) {
		// TODO Auto-generated method stub
		return false;
	}
}
