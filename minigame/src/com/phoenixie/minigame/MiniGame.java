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

	static final int CHIFFRE_MAX = 16;
	static final int CADRE_WIDTH = 6;
	static final int LINE_WIDTH = 3;
	static final int TABLE_WIDTH = 760;
	static final int FONT_SIZE = 48;

	static final int SCREEN_WIDTH = 800;
	static final int SCREEN_HEIGHT = 1280;
	static final float ASPECT_RATIO = (float) SCREEN_WIDTH
			/ (float) SCREEN_HEIGHT;

	static final int BUTTON_UNDO = 0;
	static final int BUTTON_RESET = 1;
	static final int BUTTON_RESUME = 2;
	static final int BUTTON_SETTINGS = 3;

	static final int IMG_WIDTH = 128;
	static final int BUTTON_WIDTH = 240;
	static final int BUTTON_HEIGHT = 80;
	static final int BOTTOM_SPACE = 150;

	static final Color COLEUR_BACKGROUND = new Color(0.96875f, 0.9453125f,
			0.8515625f, 1);
	static final Color COLEUR_CADRE = new Color(0.77734375f, 0.68359375f,
			0.73828125f, 1);

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Skin skin;
	private BitmapFont font;
	
	private Texture chiffreImage;
	private TextureRegion[] chiffres;
	private Texture photoImage;
	private TextureRegion[] photos;
	private Texture pictureImage;
	private TextureRegion[] pictures;
	
	private TextureRegion[] currImages;
	private ShapeRenderer renderer;
	private Rectangle viewport;

	private Texture buttonImage;

	private ImageButton buttonReset;
	private ImageButton buttonUndo;
	private ImageButton buttonResume;
	private ImageButton buttonSettings;
	private Stage stage;

	private CheckBox checkChiffres;
	private CheckBox checkPhotos;
	private CheckBox checkPictures;
	private CheckBox checkTable44;
	private CheckBox checkTable55;
	private CheckBox checkTable66;
	private Dialog dialogSettings;

	private int boiteHorzCount = 4;
	private int boiteVertCount = 4;
	private int tableWidth = 0;
	private int tableHeight = 0;
	private int boiteWidth = IMG_WIDTH;

	enum Direction {
		UP, DOWN, LEFT, RIGHT
	}

	enum BoiteType {
		CHIFFRES, PHOTOS, PICTURES
	}

	private static class Pos {
		public int x;
		public int y;
	}

	private static class GameState {
		public int boiteHorzCount;
		public int boiteVertCount;
		public int gameCount;
		public long gameScore;
		public long gameTime;
		public int[][][] gameEtapes;
		public int[][] chiffreTable;
	}

	private static class Settings {
		public BoiteType boiteType = BoiteType.CHIFFRES;
		public int boiteHorzCount = 4;
		public int boiteVertCount = 4;
	};

	private Pos tableVertex;
	private Pos tableLeftBottom;
	private Pos tableRightTop;
	private Pos[][] boitesPos;

	private int[][] chiffreTable;
	private Queue etapeQueue;
	private int gameCount;
	private long gameScore;
	private long gameTime;
	private long gameStartTime;
	private int[][][] gameEtapeBuffer;
	private GameState gameState = new GameState();
	private boolean gameLoaded = false;
	private Settings settings = new Settings();
	private Kryo kryo;

	private Pos[] videBoites;
	private int videBoitesCount = 0;

	private static Random random = new Random(System.currentTimeMillis());

	@Override
	public void create() {
		// Gdx.graphics.setContinuousRendering(false);
		Gdx.graphics.requestRendering();
		Texture.setEnforcePotImages(false);

		stage = new Stage();

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

		chiffreImage = new Texture(Gdx.files.internal("data/chiffres.png"));
		chiffres = new TextureRegion[CHIFFRE_MAX];

		for (int i = 0; i < CHIFFRE_MAX; ++i) {
			chiffres[i] = new TextureRegion(chiffreImage, i * IMG_WIDTH, 0,
					IMG_WIDTH, IMG_WIDTH);
		}

		photoImage = new Texture(Gdx.files.internal("data/photos.png"));
		photos = new TextureRegion[CHIFFRE_MAX];

		for (int i = 0; i < CHIFFRE_MAX; ++i) {
			photos[i] = new TextureRegion(photoImage, i * IMG_WIDTH, 0,
					IMG_WIDTH, IMG_WIDTH);
		}
		
		pictureImage = new Texture(Gdx.files.internal("data/pics.png"));
		pictures = new TextureRegion[CHIFFRE_MAX];

		for (int i = 0; i < CHIFFRE_MAX; ++i) {
			pictures[i] = new TextureRegion(pictureImage, i * IMG_WIDTH, 0,
					IMG_WIDTH, IMG_WIDTH);
		}
		
		tableVertex = new Pos();
		tableVertex.x = (SCREEN_WIDTH - TABLE_WIDTH) / 2;
		tableVertex.y = BOTTOM_SPACE;
		tableRightTop = new Pos();
		
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
		
		if (settings.boiteType == BoiteType.CHIFFRES) {
			currImages = chiffres;
		} else if (settings.boiteType == BoiteType.PHOTOS) {
			currImages = photos;
		} else if (settings.boiteType == BoiteType.PICTURES) {
			currImages = pictures;
		}
		
		changeTableSize(settings.boiteHorzCount);
		
		loadGame();

		drawButtons();

		createDialogSettings();

		if (gameLoaded) {
			buttonResume.setVisible(false);
			resumeGame();
		} else {
			resetGame();
		}

		Timer.schedule(new Task() {

			@Override
			public void run() {
				Gdx.graphics.requestRendering();
			}

		}, 0f, 0.5f);
	}
	
	private void changeTableSize(int size) {
		boiteHorzCount = boiteVertCount = size;
		
		boitesPos = new Pos[boiteHorzCount][boiteVertCount];
		chiffreTable = new int[boiteHorzCount][boiteVertCount];
		videBoites = new Pos[boiteHorzCount * boiteVertCount];
		
		for (int i = 0; i < boiteHorzCount * boiteVertCount; ++i) {
			videBoites[i] = new Pos();
		}
		videBoitesCount = 0;

		tableWidth = TABLE_WIDTH;
		boiteWidth = (tableWidth - 2 * CADRE_WIDTH - (boiteHorzCount - 1)
				* LINE_WIDTH)
				/ boiteHorzCount;
		tableHeight = boiteVertCount * boiteWidth + (boiteVertCount - 1)
				* LINE_WIDTH + 2 * CADRE_WIDTH;

		int yPos = tableVertex.y + CADRE_WIDTH;
		for (int j = 0; j < boiteVertCount; ++j) {
			int xPos = tableVertex.x + CADRE_WIDTH;
			for (int i = 0; i < boiteHorzCount; ++i) {
				boitesPos[i][boiteVertCount - 1 - j] = new Pos();
				boitesPos[i][boiteVertCount - 1 - j].x = xPos;
				boitesPos[i][boiteVertCount - 1 - j].y = yPos;
				xPos += boiteWidth + LINE_WIDTH;
			}
			yPos += boiteWidth + LINE_WIDTH;
		}
		
		tableLeftBottom = tableVertex;
		tableRightTop.x = tableVertex.x + tableWidth;
		tableRightTop.y = tableVertex.y + tableHeight;
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
			e.printStackTrace();
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

		gameState.boiteHorzCount = boiteHorzCount;
		gameState.boiteVertCount = boiteVertCount;
		gameState.gameCount = gameCount;
		gameState.gameScore = gameScore;
		gameState.gameTime = gameTime;
		gameState.chiffreTable = chiffreTable;
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
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private void resumeGame() {
		if (boiteHorzCount != gameState.boiteHorzCount) {
			changeTableSize(gameState.boiteHorzCount);
		}
		
		resetGame();

		gameCount = gameState.gameCount;
		gameScore = gameState.gameScore;
		gameTime = gameState.gameTime;
		chiffreTable = gameState.chiffreTable;

		for (int i = 0; i < gameState.gameEtapes.length; ++i) {
			if (gameState.gameEtapes[i] == null) {
				break;
			}
			etapeQueue.enqueue(gameState.gameEtapes[i]);
		}

		if (!etapeQueue.isEmpty()) {
			buttonUndo.setVisible(true);
			buttonReset.setVisible(true);
		}

		Gdx.graphics.requestRendering();

	}

	private void resetGame() {
		for (int j = 0; j < boiteVertCount; ++j) {
			for (int i = 0; i < boiteHorzCount; ++i) {
				chiffreTable[i][j] = -1;
			}
		}

		gameCount = 0;
		gameTime = 0;
		gameStartTime = System.currentTimeMillis();

		etapeQueue.clear();
		buttonUndo.setVisible(false);
		buttonReset.setVisible(false);

		generateChiffre();
		generateChiffre();

		shufflePhotos();

		Gdx.graphics.requestRendering();
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

	private void drawCadre() {
		renderer.begin(ShapeType.FilledRectangle);
		renderer.setColor(COLEUR_CADRE);
		renderer.filledRect(tableVertex.x, tableVertex.y, tableWidth,
				CADRE_WIDTH);
		renderer.filledRect(tableVertex.x, tableVertex.y, CADRE_WIDTH,
				tableHeight);
		renderer.filledRect(tableVertex.x + tableWidth - CADRE_WIDTH,
				tableVertex.y, CADRE_WIDTH, tableHeight);
		renderer.filledRect(tableVertex.x, tableVertex.y + tableHeight
				- CADRE_WIDTH, tableWidth, CADRE_WIDTH);

		int linepos = CADRE_WIDTH + boiteWidth;
		for (int i = 0; i < boiteHorzCount - 1; ++i) {
			renderer.filledRect(tableVertex.x + linepos, tableVertex.y,
					LINE_WIDTH, tableHeight);
			linepos += LINE_WIDTH + boiteWidth;
		}
		linepos = CADRE_WIDTH + boiteWidth;
		for (int i = 0; i < boiteVertCount - 1; ++i) {
			renderer.filledRect(tableVertex.x, tableVertex.y + linepos,
					tableWidth, LINE_WIDTH);
			linepos += LINE_WIDTH + boiteWidth;
		}

		renderer.end();
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

		buttonUp = new TextureRegion(buttonImage, BUTTON_RESUME * BUTTON_WIDTH,
				0, BUTTON_WIDTH, BUTTON_HEIGHT);
		buttonDown = new TextureRegion(buttonImage, BUTTON_RESUME
				* BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT);

		style = new ImageButtonStyle();
		style.imageUp = new TextureRegionDrawable(buttonUp);
		style.imageDown = new TextureRegionDrawable(buttonDown);
		buttonResume = new ImageButton(style);

		buttonUp = new TextureRegion(buttonImage, BUTTON_SETTINGS
				* BUTTON_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
		buttonDown = new TextureRegion(buttonImage, BUTTON_SETTINGS
				* BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT);

		style = new ImageButtonStyle();
		style.imageUp = new TextureRegionDrawable(buttonUp);
		style.imageDown = new TextureRegionDrawable(buttonDown);
		buttonSettings = new ImageButton(style);

		buttonUndo.setX(SCREEN_WIDTH - 20 - BUTTON_WIDTH);
		buttonUndo.setY(BOTTOM_SPACE + tableHeight + 40);
		buttonReset.setX(20);
		buttonReset.setY(BOTTOM_SPACE + tableHeight + 40);
		buttonResume.setX(20 + BUTTON_WIDTH + 20);
		buttonResume.setY(BOTTOM_SPACE + tableHeight + 40);
		buttonSettings.setX(SCREEN_WIDTH - 20 - BUTTON_WIDTH);
		buttonSettings.setY(SCREEN_HEIGHT - 30 - BUTTON_HEIGHT);

		stage.addActor(buttonUndo);
		stage.addActor(buttonReset);
		stage.addActor(buttonResume);
		stage.addActor(buttonSettings);

		buttonReset.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (boiteHorzCount != settings.boiteHorzCount) {
					changeTableSize(settings.boiteHorzCount);
				}
				resetGame();
			}

		});

		buttonUndo.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int[][] load = (int[][]) etapeQueue.pop();
				if (load != null) {
					chiffreTable = load;
					Gdx.graphics.requestRendering();
					if (etapeQueue.isEmpty()) {
						buttonUndo.setVisible(false);
					}
				}
			}
		});

		buttonResume.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				resumeGame();
				buttonResume.setVisible(false);
			}

		});

		buttonSettings.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				showSettings();
			}

		});
	}

	private void createDialogSettings() {
		checkChiffres = new CheckBox("Chiffres", skin);
		checkPhotos = new CheckBox("Photos", skin);
		checkPictures = new CheckBox("Images", skin);

		ButtonGroup buttonTable = new ButtonGroup(checkChiffres,
				checkPhotos, checkPictures);
		buttonTable.setMaxCheckCount(1);
		buttonTable.setMinCheckCount(1);
		buttonTable.setUncheckLast(true);
		
		checkTable44 = new CheckBox("4 X 4", skin);
		checkTable55 = new CheckBox("5 X 5", skin);
		checkTable66 = new CheckBox("6 X 6", skin);
		
		ButtonGroup tableSize = new ButtonGroup(checkTable44, checkTable55, checkTable66);
		tableSize.setMaxCheckCount(1);
		tableSize.setMinCheckCount(1);
		tableSize.setUncheckLast(true);

		dialogSettings = new Dialog("Param¨¨tre", skin) {
			protected void result(Object object) {
				if (!(Boolean) object) {
					return;
				}

				if (checkChiffres.isChecked()) {
					if (settings.boiteType != BoiteType.CHIFFRES) {
						settings.boiteType = BoiteType.CHIFFRES;
						currImages = chiffres;
						Gdx.graphics.requestRendering();
					}
				} else if (checkPhotos.isChecked()) {
					if (settings.boiteType != BoiteType.PHOTOS) {
						settings.boiteType = BoiteType.PHOTOS;
						currImages = photos;
						Gdx.graphics.requestRendering();
					}
				} else if (checkPictures.isChecked()) {
					if (settings.boiteType != BoiteType.PICTURES) {
						settings.boiteType = BoiteType.PICTURES;
						currImages = pictures;
						Gdx.graphics.requestRendering();
					}	
				}
				
				int tableSize = settings.boiteHorzCount;
				if (checkTable44.isChecked()) {
					tableSize = 4;
				} else if (checkTable55.isChecked()) {
					tableSize = 5;
				} else if (checkTable66.isChecked()) {
					tableSize = 6;
				}
				
				if (tableSize != settings.boiteHorzCount) {
					settings.boiteHorzCount = settings.boiteVertCount = tableSize;
					buttonReset.setVisible(true);
				}
				
				saveSettings();
			}
		};
		
		dialogSettings.padTop(50).padBottom(50);
		dialogSettings.getContentTable().add(checkChiffres).width(250);
		dialogSettings.getContentTable().add(checkPhotos).width(250);
		dialogSettings.getContentTable().add(checkPictures).width(250);
		dialogSettings.getContentTable().row();
		dialogSettings.getContentTable().add(checkTable44).width(250).padTop(20);
		dialogSettings.getContentTable().add(checkTable55).width(250).padTop(20);
		dialogSettings.getContentTable().add(checkTable66).width(250).padTop(20);
		dialogSettings.getContentTable().row();
		
		dialogSettings.getButtonTable().padTop(30);
		TextButton dbutton = new TextButton("OK", skin);
		dialogSettings.button(dbutton, true);
		dbutton = new TextButton("Annuler", skin);
		dialogSettings.button(dbutton, false);

		dialogSettings.invalidateHierarchy();
		dialogSettings.invalidate();
		dialogSettings.layout();
	}

	private void showSettings() {
		if (settings.boiteType == BoiteType.CHIFFRES) {
			checkChiffres.setChecked(true);
		} else {
			checkPhotos.setChecked(true);
		}
		
		switch (settings.boiteHorzCount) {
		case 4: checkTable44.setChecked(true); break;
		case 5: checkTable55.setChecked(true); break;
		case 6: checkTable66.setChecked(true); break;
		}

		dialogSettings.show(stage);
	}

	@Override
	public void dispose() {
		chiffreImage.dispose();
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
		batch.begin();

		font.setColor(1f, 1f, 1f, 0.8f);
		for (int i = 0; i < boiteHorzCount; ++i) {
			for (int j = 0; j < boiteVertCount; ++j) {
				int cur = chiffreTable[i][j];
				if (cur == -1) {
					continue;
				}
				batch.draw(currImages[cur], boitesPos[i][j].x,
						boitesPos[i][j].y, boiteWidth, boiteWidth);
				if (currImages == photos || currImages == pictures) {
					font.draw(batch, "" + (cur + 1), boitesPos[i][j].x + 10,
							boitesPos[i][j].y + FONT_SIZE);
				}
			}
		}
		drawStat();

		batch.end();

		renderer.setProjectionMatrix(camera.combined);
		drawCadre();

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

	private void shufflePhotos() {

		for (int i = 0; i < photos.length; i++) {
			int randomPosition = random.nextInt(photos.length);
			TextureRegion temp = photos[i];
			photos[i] = photos[randomPosition];
			photos[randomPosition] = temp;
		}

	}

	private void calVideBoites() {
		videBoitesCount = 0;
		for (int i = 0; i < boiteHorzCount; ++i) {
			for (int j = 0; j < boiteVertCount; ++j) {
				if (chiffreTable[i][j] == -1) {
					videBoites[videBoitesCount].x = i;
					videBoites[videBoitesCount].y = j;
					++videBoitesCount;
				}
			}
		}
	}

	private void generateChiffre() {
		calVideBoites();

		if (videBoitesCount == 0) {
			return;
		}

		int pos = random.nextInt(videBoitesCount);
		Pos boite = videBoites[pos];
		chiffreTable[boite.x][boite.y] = random.nextInt(20) == 0 ? 1 : 0; // 1 :
																			// 3
	}

	private void move(Direction direction) {
		int[][] bak = chiffreTable.clone();
		for (int i = 0; i < chiffreTable.length; ++i) {
			bak[i] = bak[i].clone();
		}

		int score = 0;
		if (direction == Direction.UP) {
			score = moveUp();
		} else if (direction == Direction.DOWN) {
			score = moveDown();
		} else if (direction == Direction.LEFT) {
			score = moveLeft();
		} else if (direction == Direction.RIGHT) {
			score = moveRight();
		}

		if (score >= 0) {
			++gameCount;
			gameScore += score;
			generateChiffre();
			etapeQueue.enqueue(bak);
			buttonUndo.setVisible(true);
			buttonReset.setVisible(true);
			buttonResume.setVisible(false);
			saveGame();
		}
	}

	private int moveRight() {
		boolean moved = false;
		int score = 0;

		for (int j = 0; j < boiteVertCount; ++j) {
			int gap = 0;
			boolean merged = false;

			if (chiffreTable[boiteHorzCount - 1][j] == -1) {
				gap = 1;
			}

			for (int i = boiteHorzCount - 1 - 1; i >= 0; --i) {
				if (chiffreTable[i][j] == -1) {
					++gap;
					continue;
				}

				int cur = chiffreTable[i][j];
				if (gap > 0) {
					chiffreTable[i + gap][j] = cur;
					chiffreTable[i][j] = -1;
					moved = true;
				}

				if (merged) {
					merged = false;
					continue;
				}

				int newi = i + gap;
				if (newi == boiteHorzCount - 1) {
					continue;
				}

				if (chiffreTable[newi + 1][j] == cur) {
					++chiffreTable[newi + 1][j];
					chiffreTable[newi][j] = -1;
					++gap;
					moved = true;
					merged = true;
					score += (1 << (chiffreTable[newi + 1][j] + 1));
				}

			}
		}
		if (!moved) {
			return -1;
		}
		return score;
	}

	private int moveLeft() {
		boolean moved = false;
		int score = 0;

		for (int j = 0; j < boiteVertCount; ++j) {
			int gap = 0;
			boolean merged = false;

			if (chiffreTable[0][j] == -1) {
				gap = 1;
			}
			for (int i = 1; i < boiteHorzCount; ++i) {
				if (chiffreTable[i][j] == -1) {
					++gap;
					continue;
				}

				int cur = chiffreTable[i][j];
				if (gap > 0) {
					chiffreTable[i - gap][j] = cur;
					chiffreTable[i][j] = -1;
					moved = true;
				}

				if (merged) {
					merged = false;
					continue;
				}

				int newi = i - gap;
				if (newi == 0) {
					continue;
				}

				if (chiffreTable[newi - 1][j] == cur) {
					++chiffreTable[newi - 1][j];
					chiffreTable[newi][j] = -1;
					++gap;
					moved = true;
					merged = true;
					score += (1 << (chiffreTable[newi - 1][j] + 1));
				}
			}
		}
		if (!moved) {
			return -1;
		}
		return score;
	}

	private int moveDown() {
		boolean moved = false;
		int score = 0;

		for (int i = 0; i < boiteHorzCount; ++i) {
			int gap = 0;
			boolean merged = false;

			if (chiffreTable[i][boiteVertCount - 1] == -1) {
				gap = 1;
			}

			for (int j = boiteVertCount - 1 - 1; j >= 0; --j) {
				if (chiffreTable[i][j] == -1) {
					++gap;
					continue;
				}

				int cur = chiffreTable[i][j];
				if (gap > 0) {
					chiffreTable[i][j + gap] = cur;
					chiffreTable[i][j] = -1;
					moved = true;
				}

				if (merged) {
					merged = false;
					continue;
				}

				int newj = j + gap;
				if (newj == boiteVertCount - 1) {
					continue;
				}

				if (chiffreTable[i][newj + 1] == cur) {
					++chiffreTable[i][newj + 1];
					chiffreTable[i][newj] = -1;
					++gap;
					moved = true;
					merged = true;
					score += (1 << (chiffreTable[i][newj + 1] + 1));
				}
			}
		}

		if (!moved) {
			return -1;
		}
		return score;
	}

	private int moveUp() {
		boolean moved = false;
		int score = 0;

		for (int i = 0; i < boiteHorzCount; ++i) {
			int gap = 0;
			boolean merged = false;

			if (chiffreTable[i][0] == -1) {
				gap = 1;
			}

			for (int j = 1; j < boiteVertCount; ++j) {
				if (chiffreTable[i][j] == -1) {
					++gap;
					continue;
				}

				int cur = chiffreTable[i][j];
				if (gap > 0) {
					chiffreTable[i][j - gap] = cur;
					chiffreTable[i][j] = -1;
					moved = true;
				}

				if (merged) {
					merged = false;
					continue;
				}

				int newj = j - gap;
				if (newj == 0) {
					continue;
				}

				if (chiffreTable[i][newj - 1] == cur) {
					++chiffreTable[i][newj - 1];
					chiffreTable[i][newj] = -1;
					++gap;
					moved = true;
					merged = true;
					score += (1 << (chiffreTable[i][newj - 1] + 1));
				}
			}
		}

		if (!moved) {
			return -1;
		}
		return score;
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
