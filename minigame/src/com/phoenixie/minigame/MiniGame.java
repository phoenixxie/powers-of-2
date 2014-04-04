package com.phoenixie.minigame;

import java.util.Random;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;

public class MiniGame extends Game implements GestureListener {
	
	static int CHIFFRE_MAX = 16;
	static int CHIFFRE_WIDTH = 128;
	static int CADRE_WIDTH = 8;
	static int LINE_WIDTH = 5;
	
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture chiffreImage;
	private TextureRegion[] chiffres;
	private ShapeRenderer renderer;
	
	private int boiteCount = 5;
	private int boiteWidth = 0;
	
	class Pos {
		public int x;
		public int y;
	}
	
	class Chiffre {
		public int x;
		public int y;
		public int chiffre = 0;
	}
	
	private Pos[][] boitesPos;
	private int[][] chiffreTable;
	
	private Pos[] videBoites;
	private int videBoitesCount = 0;
	
	private Random random = new Random(System.currentTimeMillis());
	
	@Override
	public void create() {
		
		boiteWidth = boiteCount * CHIFFRE_WIDTH + (boiteCount - 1) * LINE_WIDTH + 2 * CADRE_WIDTH;
		boitesPos = new Pos[boiteCount][boiteCount];
		chiffreTable = new int[boiteCount][boiteCount];
		videBoites = new Pos[boiteCount * boiteCount];
		
		int yPos = CADRE_WIDTH;
		for (int i = 0; i < boiteCount; ++i) {
			int xPos = CADRE_WIDTH;
			for (int j = 0; j < boiteCount; ++j) {
				boitesPos[j][boiteCount - 1 - i] = new Pos();
				boitesPos[j][boiteCount - 1 - i].x = xPos;
				boitesPos[j][boiteCount - 1 - i].y = yPos;
				xPos += CHIFFRE_WIDTH + LINE_WIDTH;
				
				chiffreTable[i][j] = -1;
			}
			yPos += CHIFFRE_WIDTH + LINE_WIDTH;
		}
		
		for (int i = 0; i < boiteCount * boiteCount; ++i) {
			videBoites[i] = new Pos();
		}
		videBoitesCount = 0;

		camera = new OrthographicCamera(boiteWidth, boiteWidth);
		camera.position.set(boiteWidth / 2, boiteWidth / 2, 0);

		batch = new SpriteBatch();
		renderer = new ShapeRenderer();
		
		chiffreImage = new Texture(Gdx.files.internal("data/chiffres.png"));
		chiffres = new TextureRegion[CHIFFRE_MAX];
		
		for (int i = 0; i < CHIFFRE_MAX; ++i) {
			chiffres[i] = new TextureRegion(chiffreImage,
					i * CHIFFRE_WIDTH, 0,
					CHIFFRE_WIDTH, CHIFFRE_WIDTH);
		}
		
		Gdx.input.setInputProcessor(new GestureDetector(0.0f, 0.0f, 0.0f, 5f, this));
		
		generateChiffre();
		generateChiffre();
	}
	
	private void drawCadre() {
		renderer.begin(ShapeType.FilledRectangle);
		renderer.setColor(1, 0, 0, 1);
		renderer.filledRect(0, 0, boiteWidth, CADRE_WIDTH);
		renderer.filledRect(0, 0, CADRE_WIDTH, boiteWidth);
		renderer.filledRect(boiteWidth - CADRE_WIDTH, 0, CADRE_WIDTH, boiteWidth);
		renderer.filledRect(0, boiteWidth - CADRE_WIDTH, boiteWidth, CADRE_WIDTH);
		
		int linepos = CADRE_WIDTH + CHIFFRE_WIDTH;
		for (int i = 0; i < boiteCount - 1; ++i) {
			renderer.filledRect(linepos, 0, LINE_WIDTH, boiteWidth);
			renderer.filledRect(0, linepos, boiteWidth, LINE_WIDTH);
			linepos += LINE_WIDTH + CHIFFRE_WIDTH;
		}
		
		renderer.end();
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
		
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		for (int i = 0; i < boiteCount; ++i) {
			for (int j = 0; j < boiteCount; ++j) {
				int cur = chiffreTable[i][j];
				if (cur == -1) {
					continue;
				}
				batch.draw(chiffres[cur],
						boitesPos[i][j].x,
						boitesPos[i][j].y);
			}
		}
		
		batch.end();
		
		renderer.setProjectionMatrix(camera.combined);
		drawCadre();
		
		if (Gdx.input.isTouched()) {
		}
		
		if (Gdx.input.isKeyPressed(Keys.LEFT)) swipeLeft();
		else if (Gdx.input.isKeyPressed(Keys.RIGHT)) swipeRight();
		else if (Gdx.input.isKeyPressed(Keys.UP)) swipeUp();
		else if (Gdx.input.isKeyPressed(Keys.DOWN)) swipeDown();
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
	
	private void calVideBoites() {
		videBoitesCount = 0;
		for (int i = 0; i < boiteCount; ++i) {
			for (int j = 0; j < boiteCount; ++j) {
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
		chiffreTable[boite.x][boite.y] = random.nextInt(4) == 0 ? 1 : 0; // 1 : 3
	}
	
	private void swipeRight() {
		boolean moved = false;
		for (int j = 0; j < boiteCount; ++j) {
			int gap = 0;
			boolean merged = false;
			
			if (chiffreTable[boiteCount - 1][j] == -1) {
				gap = 1;
			}
			
			for (int i = boiteCount - 1 - 1; i >= 0; --i) {
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
				if (newi == boiteCount - 1) {
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
		for (int j = 0; j < boiteCount; ++j) {
			int gap = 0;
			boolean merged = false;
			
			if (chiffreTable[0][j] == -1) {
				gap = 1;
			}
			for (int i = 1; i < boiteCount; ++i) {
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
		for (int i = 0; i < boiteCount; ++i) {
			int gap = 0;
			boolean merged = false;
			
			if (chiffreTable[i][boiteCount - 1] == -1) {
				gap = 1;
			}
			
			for (int j = boiteCount - 1 - 1; j >= 0; --j) {
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
				if (newj == boiteCount - 1) {
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
		for (int i = 0; i < boiteCount; ++i) {
			int gap = 0;
			boolean merged = false;
			
			if (chiffreTable[i][0] == -1) {
				gap = 1;
			}
			
			for (int j = 1; j < boiteCount; ++j) {
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
