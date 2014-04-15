package com.phoenixie.minigame;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.phoenixie.minigame.MiniGame.Pos;
import com.phoenixie.minigame.MiniGame.Direction;;

public class Grille {
	static final int CHIFFRE_MAX = 16;

	static final int CADRE_WIDTH = 6;
	static final int LINE_WIDTH = 3;
	static final int TABLE_WIDTH = 760;

	private static Random random = new Random(System.currentTimeMillis());

	private Tuile[] tuileStack;
	private int tuileStackTop;

	private int maxChiffre;
	private int tableSize = 4;

	private int boiteWidth;

	private Pos tableVertex;
	private Pos[][] boitesPos;

	private int[][] chiffreTable;
	private Tuile[][] tuileTable;
	private Pos[] videBoites;
	private int videBoitesCount = 0;

	public Grille() {

	}

	public void create(Pos tableVertex) {
		this.tableVertex = tableVertex;
	}

	public int getTableSize() {
		return tableSize;
	}

	public void setTableSize(int size) {
		tableSize = size;

		boitesPos = new Pos[tableSize][tableSize];
		
		chiffreTable = new int[tableSize][tableSize];
		videBoites = new Pos[tableSize * tableSize];

		for (int i = 0; i < tableSize * tableSize; ++i) {
			videBoites[i] = new Pos();
		}
		videBoitesCount = 0;
		
		tuileTable = new Tuile[tableSize][tableSize];
		tuileStack = new Tuile[tableSize * tableSize];
		
		boiteWidth = (TABLE_WIDTH - 2 * CADRE_WIDTH - (tableSize - 1)
				* LINE_WIDTH)
				/ tableSize;
		// tableHeight = tableSize * boiteWidth + (tableSize - 1)
		// * LINE_WIDTH + 2 * CADRE_WIDTH;

		int yPos = tableVertex.y + CADRE_WIDTH;
		for (int j = 0; j < tableSize; ++j) {
			int xPos = tableVertex.x + CADRE_WIDTH;
			for (int i = 0; i < tableSize; ++i) {
				boitesPos[i][tableSize - 1 - j] = new Pos();
				boitesPos[i][tableSize - 1 - j].x = xPos;
				boitesPos[i][tableSize - 1 - j].y = yPos;
				xPos += boiteWidth + LINE_WIDTH;
			}
			yPos += boiteWidth + LINE_WIDTH;
		}

		reset();
	}

	public void reset() {
		for (int j = 0; j < tableSize; ++j) {
			for (int i = 0; i < tableSize; ++i) {
				chiffreTable[i][j] = -1;
				tuileTable[i][j] = null;
			}
		}
		tuileStackTop = 0;
		for (int i = 0; i < tuileStack.length; ++i) {
			Tuile t = new Tuile();
			tuileStack[tuileStackTop++] = t;
		}
		
		maxChiffre = 0;

		generateChiffre();
		generateChiffre();
	}
	
	public void draw(ShapeRenderer renderer, SpriteBatch batch) {
		renderer.begin(ShapeType.FilledRectangle);
		renderer.setColor(MiniGame.COLEUR_CADRE);
		renderer.filledRect(tableVertex.x, tableVertex.y, TABLE_WIDTH,
				CADRE_WIDTH);
		renderer.filledRect(tableVertex.x, tableVertex.y, CADRE_WIDTH,
				TABLE_WIDTH);
		renderer.filledRect(tableVertex.x + TABLE_WIDTH - CADRE_WIDTH,
				tableVertex.y, CADRE_WIDTH, TABLE_WIDTH);
		renderer.filledRect(tableVertex.x, tableVertex.y + TABLE_WIDTH
				- CADRE_WIDTH, TABLE_WIDTH, CADRE_WIDTH);

		int linepos = CADRE_WIDTH + boiteWidth;
		for (int i = 0; i < tableSize - 1; ++i) {
			renderer.filledRect(tableVertex.x + linepos, tableVertex.y,
					LINE_WIDTH, TABLE_WIDTH);
			linepos += LINE_WIDTH + boiteWidth;
		}
		linepos = CADRE_WIDTH + boiteWidth;
		for (int i = 0; i < tableSize - 1; ++i) {
			renderer.filledRect(tableVertex.x, tableVertex.y + linepos,
					TABLE_WIDTH, LINE_WIDTH);
			linepos += LINE_WIDTH + boiteWidth;
		}

		renderer.end();

		batch.begin();
//		font.setColor(1f, 1f, 1f, 0.8f);
//		for (int i = 0; i < boiteHorzCount; ++i) {
//			for (int j = 0; j < boiteHorzCount; ++j) {
//				int cur = chiffreTable[i][j];
//				if (cur == -1) {
//					continue;
//				}
//				
//			}
//		}
		batch.end();
	}

	public Tuile getTuile() {
		return tuileStack[--tuileStackTop];
	}

	public void releaseTuile(Tuile t) {
		tuileStack[tuileStackTop++] = t;
	}

	private void calVideBoites() {
		videBoitesCount = 0;
		for (int i = 0; i < tableSize; ++i) {
			for (int j = 0; j < tableSize; ++j) {
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
		chiffreTable[boite.x][boite.y] = random.nextInt(20) == 0 ? 1 : 0;
	}

	public void move(Direction direction) {
//		int[][] bak = chiffreTable.clone();
//		for (int i = 0; i < chiffreTable.length; ++i) {
//			bak[i] = bak[i].clone();
//		}

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
//			++gameCount;
//			gameScore += score;
			generateChiffre();
//			etapeQueue.enqueue(bak);
//			buttonUndo.setVisible(true);
//			buttonReset.setVisible(true);
//			saveGame();
		}
	}

	private int moveRight() {
		boolean moved = false;
		int score = 0;

		for (int j = 0; j < tableSize; ++j) {
			int gap = 0;
			boolean merged = false;

			if (chiffreTable[tableSize - 1][j] == -1) {
				gap = 1;
			}

			for (int i = tableSize - 1 - 1; i >= 0; --i) {
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
				if (newi == tableSize - 1) {
					continue;
				}

				if (chiffreTable[newi + 1][j] == cur && chiffreTable[newi + 1][j] < CHIFFRE_MAX) {
					++chiffreTable[newi + 1][j];
					chiffreTable[newi][j] = -1;
					++gap;
					moved = true;
					merged = true;
					score += (1 << (chiffreTable[newi + 1][j] + 1));
//					felicitation(chiffreTable[newi + 1][j]);
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

		for (int j = 0; j < tableSize; ++j) {
			int gap = 0;
			boolean merged = false;

			if (chiffreTable[0][j] == -1) {
				gap = 1;
			}
			for (int i = 1; i < tableSize; ++i) {
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

				if (chiffreTable[newi - 1][j] == cur && chiffreTable[newi - 1][j] < CHIFFRE_MAX) {
					++chiffreTable[newi - 1][j];
					chiffreTable[newi][j] = -1;
					++gap;
					moved = true;
					merged = true;
					score += (1 << (chiffreTable[newi - 1][j] + 1));
//					felicitation(chiffreTable[newi - 1][j]);
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

		for (int i = 0; i < tableSize; ++i) {
			int gap = 0;
			boolean merged = false;

			if (chiffreTable[i][tableSize - 1] == -1) {
				gap = 1;
			}

			for (int j = tableSize - 1 - 1; j >= 0; --j) {
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
				if (newj == tableSize - 1) {
					continue;
				}

				if (chiffreTable[i][newj + 1] == cur && chiffreTable[i][newj + 1] < CHIFFRE_MAX) {
					++chiffreTable[i][newj + 1];
					chiffreTable[i][newj] = -1;
					++gap;
					moved = true;
					merged = true;
					score += (1 << (chiffreTable[i][newj + 1] + 1));
//					felicitation(chiffreTable[i][newj + 1]);
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

		for (int i = 0; i < tableSize; ++i) {
			int gap = 0;
			boolean merged = false;

			if (chiffreTable[i][0] == -1) {
				gap = 1;
			}

			for (int j = 1; j < tableSize; ++j) {
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

				if (chiffreTable[i][newj - 1] == cur && chiffreTable[i][newj - 1] < CHIFFRE_MAX) {
					++chiffreTable[i][newj - 1];
					chiffreTable[i][newj] = -1;
					++gap;
					moved = true;
					merged = true;
					score += (1 << (chiffreTable[i][newj - 1] + 1));
//					felicitation(chiffreTable[i][newj - 1]);
				}
			}
		}

		if (!moved) {
			return -1;
		}
		return score;
	}
	

	public class Tuile {

		private int chiffre = -1;

		private int posx;
		private int posy;

		private int nextx;
		private int nexty;

		private Direction direction = Direction.HOLD;

		private Tuile() {
			init(0, 0, -1);
		}

		public boolean isMoving() {
			return (direction != Direction.HOLD);
		}

		public Tuile init(int posx, int posy) {
			return init(posx, posy, 0);
		}

		public Tuile init(int posx, int posy, int chiffre) {
			this.chiffre = chiffre;

			this.posx = posx;
			this.posy = posy;

			this.nextx = 0;
			this.nexty = 0;

			this.direction = Direction.HOLD;

			return this;
		}

		public Tuile move(Direction direction, int nextx, int nexty) {

			this.direction = direction;
			this.nextx = boitesPos[nextx][nexty].x;
			this.nexty = boitesPos[nextx][nexty].y;

			return this;
		}

		public Tuile draw(SpriteBatch batch, ImageStore store) {

			if (direction == Direction.RIGHT) {
				posx += MiniGame.ANIME_SPEED;
				if (posx > nextx) {
					posx = nextx;
					direction = Direction.HOLD;
				}
			} else if (direction == Direction.LEFT) {
				posx -= MiniGame.ANIME_SPEED;
				if (posx < nextx) {
					posx = nextx;
					direction = Direction.HOLD;
				}
			} else if (direction == Direction.DOWN) {
				posy += MiniGame.ANIME_SPEED;
				if (posy > nexty) {
					posy = nexty;
					direction = Direction.HOLD;
				}
			} else if (direction == Direction.UP) {
				posy -= MiniGame.ANIME_SPEED;
				if (posy < nexty) {
					posy = nexty;
					direction = Direction.HOLD;
				}
			}

			store.draw(batch, chiffre, posx, posy);

			return this;
		}

		public int chiffre() {
			return chiffre;
		}

		public boolean merge(Tuile autre) {
			if (this.chiffre != autre.chiffre) {
				return false;
			}
			if (isMoving() || autre.isMoving()) {
				return false;
			}
			if (this.chiffre == CHIFFRE_MAX) {
				return false;
			}

			++chiffre;
			return true;
		}
	}
}
