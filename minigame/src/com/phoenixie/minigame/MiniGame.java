package com.phoenixie.minigame;

import java.util.Random;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;



public class MiniGame extends Game implements GestureListener {
	
	static final int CHIFFRE_MAX = 16;
	static final int CADRE_WIDTH = 6;
	static final int LINE_WIDTH = 3;
	static final int TABLE_WIDTH = 760;
	
	static final int SCREEN_WIDTH = 800;
	static final int SCREEN_HEIGHT = 1280;
	static final float ASPECT_RATIO = (float)SCREEN_WIDTH / (float)SCREEN_HEIGHT;
	
	static final int BUTTON_RESET = 0;
	static final int BUTTON_BACK = 1;
	
	static final int IMG_WIDTH = 128;
	static final int BUTTON_WIDTH = 200;
	static final int BUTTON_HEIGHT = 80;
	
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture chiffreImage;
	private TextureRegion[] chiffres;
	private ShapeRenderer renderer;
	private Rectangle viewport;
	
	private Texture buttonImage;

	private ImageButton buttonBack;
	private ImageButton buttonReset;
	private Stage stage;

	private int boiteHorzCount = 5;
	private int boiteVertCount = 5;
	private int tableWidth = 0;
	private int tableHeight = 0;
	private int boiteWidth = IMG_WIDTH;
	
	class Pos {
		public int x;
		public int y;
	}
	
	class Chiffre {
		public int x;
		public int y;
		public int chiffre = 0;
	}
	
	private Pos tableVertex;
	private Pos[][] boitesPos;
	private int[][] chiffreTable;
	
	private Pos[] videBoites;
	private int videBoitesCount = 0;
	
	private Random random = new Random(System.currentTimeMillis());
	
	@Override
	public void create() {
		Gdx.graphics.setContinuousRendering(false);
		Gdx.graphics.requestRendering();
		Texture.setEnforcePotImages(false);
		
		stage = new Stage();
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, new GestureDetector(0.0f, 0.0f, 0.0f, 5f, this)));

		boitesPos = new Pos[boiteHorzCount][boiteVertCount];
		chiffreTable = new int[boiteHorzCount][boiteVertCount];
		videBoites = new Pos[boiteHorzCount * boiteVertCount];
		
		for (int i = 0; i < boiteHorzCount * boiteVertCount; ++i) {
			videBoites[i] = new Pos();
		}
		videBoitesCount = 0;

		camera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
		camera.position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
		stage.setCamera(camera);

		batch = new SpriteBatch();
		renderer = new ShapeRenderer();
		
		chiffreImage = new Texture(Gdx.files.internal("data/chiffres.png"));
		chiffres = new TextureRegion[CHIFFRE_MAX];
		
		for (int i = 0; i < CHIFFRE_MAX; ++i) {
			chiffres[i] = new TextureRegion(chiffreImage,
					i * IMG_WIDTH, 0,
					IMG_WIDTH, IMG_WIDTH);
		}
			
		reset();
		
		drawButtons();	
	}
	
	private void reset() {
		tableWidth = TABLE_WIDTH;
		boiteWidth = (tableWidth - 2 * CADRE_WIDTH - (boiteHorzCount - 1) * LINE_WIDTH) / boiteHorzCount;
		tableHeight = boiteVertCount * boiteWidth + (boiteVertCount - 1) * LINE_WIDTH + 2 * CADRE_WIDTH;
		
		tableVertex = new Pos();
		tableVertex.x = (SCREEN_WIDTH - TABLE_WIDTH) / 2;
		tableVertex.y = 20;
		
		int yPos = tableVertex.y + CADRE_WIDTH;
		for (int j = 0; j < boiteVertCount; ++j) {
			int xPos = tableVertex.x + CADRE_WIDTH;
			for (int i = 0; i < boiteHorzCount; ++i) {
				boitesPos[i][boiteVertCount - 1 - j] = new Pos();
				boitesPos[i][boiteVertCount - 1 - j].x = xPos;
				boitesPos[i][boiteVertCount - 1 - j].y = yPos;
				xPos += boiteWidth + LINE_WIDTH;
				
				chiffreTable[i][j] = -1;
			}
			yPos += boiteWidth + LINE_WIDTH;
		}
		
		generateChiffre();
		generateChiffre();
		
		Gdx.graphics.requestRendering();
	}
	
	private void drawCadre() {
		renderer.begin(ShapeType.FilledRectangle);
		renderer.setColor(0, 1, 0, 1);
		renderer.filledRect(tableVertex.x, tableVertex.y, tableWidth, CADRE_WIDTH);
		renderer.filledRect(tableVertex.x, tableVertex.y, CADRE_WIDTH, tableHeight);
		renderer.filledRect(tableVertex.x + tableWidth - CADRE_WIDTH, tableVertex.y, CADRE_WIDTH, tableHeight);
		renderer.filledRect(tableVertex.x, tableVertex.y + tableHeight - CADRE_WIDTH, tableWidth, CADRE_WIDTH);
		
		int linepos = CADRE_WIDTH + boiteWidth;
		for (int i = 0; i < boiteHorzCount - 1; ++i) {
			renderer.filledRect(tableVertex.x + linepos, tableVertex.y, LINE_WIDTH, tableHeight);
			linepos += LINE_WIDTH + boiteWidth;
		}
		linepos = CADRE_WIDTH + boiteWidth;
		for (int i = 0; i < boiteVertCount - 1; ++i) {
			renderer.filledRect(tableVertex.x, tableVertex.y + linepos, tableWidth, LINE_WIDTH);
			linepos += LINE_WIDTH + boiteWidth;
		}
		
		renderer.end();
	}
	
	private void drawButtons() {
		buttonImage = new Texture(Gdx.files.internal("data/buttons.png"));
		TextureRegion buttonBackUp = new TextureRegion(buttonImage, 0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
		TextureRegion buttonBackDown = new TextureRegion(buttonImage, 0, BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT);
		
		ImageButtonStyle style = new ImageButtonStyle();
		style.imageUp = new TextureRegionDrawable(buttonBackUp);
		style.imageDown = new TextureRegionDrawable(buttonBackDown);
		buttonBack = new ImageButton(style);
		
		buttonBackUp = new TextureRegion(buttonImage, BUTTON_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
		buttonBackDown = new TextureRegion(buttonImage, BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT);
		
		style = new ImageButtonStyle();
		style.imageUp = new TextureRegionDrawable(buttonBackUp);
		style.imageDown = new TextureRegionDrawable(buttonBackDown);
		buttonReset = new ImageButton(style);
		
		buttonBack.setX(SCREEN_WIDTH - 20 - BUTTON_WIDTH);
		buttonBack.setY(tableHeight + 20 + 50);
		buttonReset.setX(SCREEN_WIDTH - 2 * (20 + BUTTON_WIDTH));
		buttonReset.setY(tableHeight + 20 + 50);
		
		System.out.println(buttonBack.getX());

		stage.addActor(buttonBack);
		stage.addActor(buttonReset);

		buttonReset.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				reset();
			}
			
		});
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
		
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		stage.draw();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		for (int i = 0; i < boiteHorzCount; ++i) {
			for (int j = 0; j < boiteVertCount; ++j) {
				int cur = chiffreTable[i][j];
				if (cur == -1) {
					continue;
				}
				batch.draw(chiffres[cur],
						boitesPos[i][j].x,
						boitesPos[i][j].y,
						boiteWidth,
						boiteWidth);
			}
		}
		batch.end();
		
		renderer.setProjectionMatrix(camera.combined);
		drawCadre();
		
		if (Gdx.input.isKeyPressed(Keys.LEFT)) swipeLeft();
		else if (Gdx.input.isKeyPressed(Keys.RIGHT)) swipeRight();
		else if (Gdx.input.isKeyPressed(Keys.UP)) swipeUp();
		else if (Gdx.input.isKeyPressed(Keys.DOWN)) swipeDown();
	}

	@Override
	public void resize(int width, int height) {
		float aspectRatio = (float)width/(float)height;
	    float scale = 1f;
	    Vector2 crop = new Vector2(0f, 0f);
	    
	    if (aspectRatio > ASPECT_RATIO) {
            scale = (float)height / (float)SCREEN_HEIGHT;
            crop.x = (width - SCREEN_WIDTH * scale) / 2f;
        } else if (aspectRatio < ASPECT_RATIO) {
            scale = (float)width / (float)SCREEN_WIDTH;
            crop.y = (height - SCREEN_HEIGHT * scale) / 2f;
        } else {
            scale = (float)width / (float)SCREEN_WIDTH;
        }

        float w = (float)SCREEN_WIDTH * scale;
        float h = (float)SCREEN_HEIGHT * scale;
        viewport = new Rectangle(crop.x, crop.y, w, h);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
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
		chiffreTable[boite.x][boite.y] = random.nextInt(20) == 0 ? 1 : 0; // 1 : 3
	}
	
	private void swipeRight() {
		boolean moved = false;
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
				}

			}
		}
		if (moved) {
			generateChiffre();
		}	
	}

	private void swipeLeft() {
		boolean moved = false;
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
				}
			}
		}
		if (moved) {
			generateChiffre();
		}
	}
	
	private void swipeDown() {
		boolean moved = false;
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
				}
			}
		}
		if (moved) {
			generateChiffre();
		}		
	}

	private void swipeUp() {
		boolean moved = false;
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
				}
			}
		}
		if (moved) {
			generateChiffre();
		}
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
			if (velocityX > 0) {
				swipeRight();
			} else {
				swipeLeft();
			}
		} else {
			if (velocityY > 0) {
				swipeDown();
			} else {
				swipeUp();
			}
		}
		
		return true;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		// TODO Auto-generated method stub
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
