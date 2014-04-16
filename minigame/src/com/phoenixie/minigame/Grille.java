package com.phoenixie.minigame;

import java.util.Random;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.phoenixie.minigame.MiniGame.Pos;
import com.phoenixie.minigame.MiniGame.Direction;

public class Grille {
	static final int CHIFFRE_MAX = 16;

	static final int CADRE_WIDTH = 6;
	static final int LINE_WIDTH = 3;
	static final int TABLE_WIDTH = 760;

	private static Random random = new Random(System.currentTimeMillis());

	private MiniGame game;

	private BitmapFont font;
	private ImageStore store;

	private Tuile[] tuileStack;
	private int tuileStackTop;

	private Tuile[] tuilePoubelle;
	private int tuilePoubelleCount;

	private int maxChiffre;
	private int tableSize = 4;

	private int boiteWidth;
	private int tableWidth;

	private Pos tableVertex;
	private Pos[][] boitesPos;

	private int[][][] chiffreTable;
	private Tuile[][][] tuileTable;
	private int workingTable;

	private Pos[] videBoites;
	private int videBoitesCount = 0;

	private boolean isMoving = false;
	private int deltaScore = 0;

	public Grille() {

	}

	public void create(MiniGame game, Pos tableVertex, BitmapFont font,
			ImageStore store) {
		this.game = game;
		this.tableVertex = tableVertex;
		this.font = font;
		this.store = store;
	}

	public int getTableSize() {
		return tableSize;
	}

	public void setTableSize(int size) {
		tableSize = size;

		boitesPos = new Pos[tableSize][tableSize];

		chiffreTable = new int[2][tableSize][tableSize];
		videBoites = new Pos[tableSize * tableSize];

		for (int i = 0; i < tableSize * tableSize; ++i) {
			videBoites[i] = new Pos();
		}
		videBoitesCount = 0;

		tuileTable = new Tuile[2][tableSize][tableSize];
		tuileStack = new Tuile[2 * tableSize * tableSize];
		tuilePoubelle = new Tuile[2 * tableSize * tableSize];

		boiteWidth = (TABLE_WIDTH - 2 * CADRE_WIDTH - (tableSize - 1)
				* LINE_WIDTH)
				/ tableSize;
		tableWidth = tableSize * boiteWidth + (tableSize - 1) * LINE_WIDTH + 2
				* CADRE_WIDTH;

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

	public void restart() {
		reset();

		generateChiffre();
		generateChiffre();
	}

	public void reset() {
		for (int j = 0; j < tableSize; ++j) {
			for (int i = 0; i < tableSize; ++i) {
				chiffreTable[0][i][j] = chiffreTable[1][i][j] = -1;
				tuileTable[0][i][j] = tuileTable[1][i][j] = null;
			}
		}
		workingTable = 0;

		tuileStackTop = 0;
		for (int i = 0; i < tuileStack.length; ++i) {
			Tuile t = new Tuile();
			tuileStack[tuileStackTop] = t;
			tuileStackTop++;
		}

		maxChiffre = 0;

		isMoving = false;
		deltaScore = 0;
		tuilePoubelleCount = 0;
	}

	public void setChiffreTable(int[][] table) {
		reset();

		chiffreTable[workingTable] = table;
		for (int j = 0; j < tableSize; ++j) {
			for (int i = 0; i < tableSize; ++i) {
				int cur = table[i][j];
				if (cur != -1) {
					tuileTable[workingTable][i][j] = getTuile().init(i, j, cur);
					if (maxChiffre < cur) {
						maxChiffre = cur;
					}
				}
			}
		}
	}

	public void draw(ShapeRenderer renderer, SpriteBatch batch) {
		renderer.begin(ShapeType.FilledRectangle);
		renderer.setColor(MiniGame.COLEUR_CADRE);
		renderer.filledRect(tableVertex.x, tableVertex.y, tableWidth,
				CADRE_WIDTH);
		renderer.filledRect(tableVertex.x, tableVertex.y, CADRE_WIDTH,
				tableWidth);
		renderer.filledRect(tableVertex.x + tableWidth - CADRE_WIDTH,
				tableVertex.y, CADRE_WIDTH, tableWidth);
		renderer.filledRect(tableVertex.x, tableVertex.y + tableWidth
				- CADRE_WIDTH, tableWidth, CADRE_WIDTH);

		int linepos = CADRE_WIDTH + boiteWidth;
		for (int i = 0; i < tableSize - 1; ++i) {
			renderer.filledRect(tableVertex.x + linepos, tableVertex.y,
					LINE_WIDTH, tableWidth);
			linepos += LINE_WIDTH + boiteWidth;
		}
		linepos = CADRE_WIDTH + boiteWidth;
		for (int i = 0; i < tableSize - 1; ++i) {
			renderer.filledRect(tableVertex.x, tableVertex.y + linepos,
					tableWidth, LINE_WIDTH);
			linepos += LINE_WIDTH + boiteWidth;
		}

		renderer.end();

		int movingCount = 0;
		batch.begin();
		font.setColor(1f, 1f, 1f, 0.8f);
		for (int i = 0; i < tableSize; ++i) {
			for (int j = 0; j < tableSize; ++j) {
				Tuile cur = tuileTable[workingTable][i][j];
				if (cur == null) {
					continue;
				}
				cur.draw(batch, store);

				if (cur.isMoving()) {
					++movingCount;
				}
			}
		}
		batch.end();

		if (isMoving && movingCount == 0) {
			isMoving = false;
			workingTable = 1 - workingTable;

			game.onMoved(maxChiffre, deltaScore,
					chiffreTable[1 - workingTable], chiffreTable[workingTable]);

			clearPoubelle();
			generateChiffre();
		}
	}

	public Tuile getTuile() {
		--tuileStackTop;
		return tuileStack[tuileStackTop];
	}

	public void releaseTuile(Tuile t) {
		tuileStack[tuileStackTop] = t;
		++tuileStackTop;
	}

	public void putInPoubelle(Tuile t) {
		tuilePoubelle[tuilePoubelleCount] = t;
		++tuilePoubelleCount;
	}

	public void clearPoubelle() {
		for (int i = 0; i < tuilePoubelleCount; ++i) {
			releaseTuile(tuilePoubelle[i]);
		}
		tuilePoubelleCount = 0;
	}

	private void calVideBoites() {
		videBoitesCount = 0;
		for (int i = 0; i < tableSize; ++i) {
			for (int j = 0; j < tableSize; ++j) {
				if (chiffreTable[workingTable][i][j] == -1) {
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

		Tuile t = getTuile();

		int v = random.nextInt(20) == 0 ? 1 : 0;
		t.init(boite.x, boite.y, v);

		chiffreTable[workingTable][boite.x][boite.y] = v;
		tuileTable[workingTable][boite.x][boite.y] = t;
	}

	public boolean move(Direction direction) {
		if (isMoving) {
			return false;
		}

		for (int j = 0; j < tableSize; ++j) {
			for (int i = 0; i < tableSize; ++i) {
				chiffreTable[1 - workingTable][i][j] = chiffreTable[workingTable][i][j];
				tuileTable[1 - workingTable][i][j] = tuileTable[workingTable][i][j];
			}
		}

		deltaScore = 0;
		tuilePoubelleCount = 0;

		boolean moved = false;

		if (direction == Direction.UP) {
			moved = moveUp();
		} else if (direction == Direction.DOWN) {
			moved = moveDown();
		} else if (direction == Direction.LEFT) {
			moved = moveLeft();
		} else if (direction == Direction.RIGHT) {
			moved = moveRight();
		}

		if (moved) {
			isMoving = true;
		}

		return moved;
	}

	private boolean moveRight() {
		boolean moved = false;

		for (int j = 0; j < tableSize; ++j) {
			int gap = 0;
			boolean merged = false;

			if (chiffreTable[1 - workingTable][tableSize - 1][j] == -1) {
				gap = 1;
			}

			for (int i = tableSize - 1 - 1; i >= 0; --i) {
				if (chiffreTable[1 - workingTable][i][j] == -1) {
					++gap;
					continue;
				}

				int cur = chiffreTable[1 - workingTable][i][j];
				if (gap > 0) {
					chiffreTable[1 - workingTable][i + gap][j] = cur;
					chiffreTable[1 - workingTable][i][j] = -1;
					moved = true;
				}

				int newi = i + gap;
				if (!merged
						&& newi < tableSize - 1
						&& chiffreTable[1 - workingTable][newi + 1][j] == cur
						&& chiffreTable[1 - workingTable][newi + 1][j] < CHIFFRE_MAX) {
					int value = ++chiffreTable[1 - workingTable][newi + 1][j];
					chiffreTable[1 - workingTable][newi][j] = -1;
					++gap;
					moved = true;
					merged = true;
					deltaScore += (1 << (chiffreTable[1 - workingTable][newi + 1][j] + 1));

					tuileTable[workingTable][i][j].move(Direction.RIGHT,
							newi + 1, j);

					putInPoubelle(tuileTable[1 - workingTable][i][j]);
					putInPoubelle(tuileTable[1 - workingTable][newi + 1][j]);

					tuileTable[1 - workingTable][i][j] = null;
					tuileTable[1 - workingTable][newi + 1][j] = getTuile()
							.init(newi + 1, j, value);

					if (maxChiffre < value) {
						maxChiffre = value;
					}
				} else {
					merged = false;
					if (gap > 0) {
						tuileTable[workingTable][i][j].move(Direction.RIGHT,
								newi, j);

						tuileTable[1 - workingTable][newi][j] = tuileTable[1 - workingTable][i][j];
						tuileTable[1 - workingTable][i][j] = null;
					}
				}
			}
		}

		return moved;
	}

	private boolean moveLeft() {
		boolean moved = false;

		for (int j = 0; j < tableSize; ++j) {
			int gap = 0;
			boolean merged = false;

			if (chiffreTable[1 - workingTable][0][j] == -1) {
				gap = 1;
			}
			for (int i = 1; i < tableSize; ++i) {
				if (chiffreTable[1 - workingTable][i][j] == -1) {
					++gap;
					continue;
				}

				int cur = chiffreTable[1 - workingTable][i][j];
				if (gap > 0) {
					chiffreTable[1 - workingTable][i - gap][j] = cur;
					chiffreTable[1 - workingTable][i][j] = -1;
					moved = true;
				}

				int newi = i - gap;
				if (!merged
						&& newi > 0
						&& chiffreTable[1 - workingTable][newi - 1][j] == cur
						&& chiffreTable[1 - workingTable][newi - 1][j] < CHIFFRE_MAX) {
					int value = ++chiffreTable[1 - workingTable][newi - 1][j];
					chiffreTable[1 - workingTable][newi][j] = -1;
					++gap;
					moved = true;
					merged = true;
					deltaScore += (1 << (chiffreTable[1 - workingTable][newi - 1][j] + 1));
					tuileTable[workingTable][i][j].move(Direction.LEFT,
							newi - 1, j);

					putInPoubelle(tuileTable[1 - workingTable][i][j]);
					putInPoubelle(tuileTable[1 - workingTable][newi - 1][j]);

					tuileTable[1 - workingTable][newi - 1][j] = getTuile()
							.init(newi - 1, j, value);
					tuileTable[1 - workingTable][i][j] = null;

					if (maxChiffre < value) {
						maxChiffre = value;
					}
				} else {
					merged = false;
					if (gap > 0) {
						tuileTable[workingTable][i][j].move(Direction.LEFT,
								newi, j);

						tuileTable[1 - workingTable][newi][j] = tuileTable[1 - workingTable][i][j];
						tuileTable[1 - workingTable][i][j] = null;
					}
				}
			}
		}
		return moved;
	}

	private boolean moveDown() {
		boolean moved = false;

		for (int i = 0; i < tableSize; ++i) {
			int gap = 0;
			boolean merged = false;

			if (chiffreTable[1 - workingTable][i][tableSize - 1] == -1) {
				gap = 1;
			}

			for (int j = tableSize - 1 - 1; j >= 0; --j) {
				if (chiffreTable[1 - workingTable][i][j] == -1) {
					++gap;
					continue;
				}

				int cur = chiffreTable[1 - workingTable][i][j];
				if (gap > 0) {
					chiffreTable[1 - workingTable][i][j + gap] = cur;
					chiffreTable[1 - workingTable][i][j] = -1;
					moved = true;
				}

				int newj = j + gap;
				if (!merged
						&& newj < tableSize - 1
						&& chiffreTable[1 - workingTable][i][newj + 1] == cur
						&& chiffreTable[1 - workingTable][i][newj + 1] < CHIFFRE_MAX) {
					int value = ++chiffreTable[1 - workingTable][i][newj + 1];
					chiffreTable[1 - workingTable][i][newj] = -1;
					++gap;
					moved = true;
					merged = true;
					deltaScore += (1 << (chiffreTable[1 - workingTable][i][newj + 1] + 1));
					tuileTable[workingTable][i][j].move(Direction.DOWN, i,
							newj + 1);

					putInPoubelle(tuileTable[1 - workingTable][i][j]);
					putInPoubelle(tuileTable[1 - workingTable][i][newj + 1]);

					tuileTable[1 - workingTable][i][newj + 1] = getTuile()
							.init(i, newj + 1, value);
					tuileTable[1 - workingTable][i][j] = null;

					if (maxChiffre < value) {
						maxChiffre = value;
					}
				} else {
					merged = false;
					if (gap > 0) {
						tuileTable[workingTable][i][j].move(Direction.DOWN, i,
								newj);

						tuileTable[1 - workingTable][i][newj] = tuileTable[1 - workingTable][i][j];
						tuileTable[1 - workingTable][i][j] = null;
					}
				}
			}
		}

		return moved;
	}

	private boolean moveUp() {
		boolean moved = false;

		for (int i = 0; i < tableSize; ++i) {
			int gap = 0;
			boolean merged = false;

			if (chiffreTable[1 - workingTable][i][0] == -1) {
				gap = 1;
			}

			for (int j = 1; j < tableSize; ++j) {
				if (chiffreTable[1 - workingTable][i][j] == -1) {
					++gap;
					continue;
				}

				int cur = chiffreTable[1 - workingTable][i][j];
				if (gap > 0) {
					chiffreTable[1 - workingTable][i][j - gap] = cur;
					chiffreTable[1 - workingTable][i][j] = -1;
					moved = true;
				}

				int newj = j - gap;
				if (!merged
						&& newj > 0
						&& chiffreTable[1 - workingTable][i][newj - 1] == cur
						&& chiffreTable[1 - workingTable][i][newj - 1] < CHIFFRE_MAX) {
					int value = ++chiffreTable[1 - workingTable][i][newj - 1];
					chiffreTable[1 - workingTable][i][newj] = -1;
					++gap;
					moved = true;
					merged = true;
					deltaScore += (1 << (chiffreTable[1 - workingTable][i][newj - 1] + 1));
					tuileTable[workingTable][i][j].move(Direction.UP, i,
							newj - 1);

					putInPoubelle(tuileTable[1 - workingTable][i][j]);
					putInPoubelle(tuileTable[1 - workingTable][i][newj - 1]);

					tuileTable[1 - workingTable][i][newj - 1] = getTuile()
							.init(i, newj - 1, value);
					tuileTable[1 - workingTable][i][j] = null;

					if (maxChiffre < value) {
						maxChiffre = value;
					}
				} else {
					merged = false;
					if (gap > 0) {
						tuileTable[workingTable][i][j].move(Direction.UP, i,
								newj);

						tuileTable[1 - workingTable][i][newj] = tuileTable[1 - workingTable][i][j];
						tuileTable[1 - workingTable][i][j] = null;
					}
				}
			}
		}

		return moved;
	}

	public int point(int x, int y) {
		int[][] table = chiffreTable[workingTable];

		if (x > tableVertex.x + TABLE_WIDTH || x < tableVertex.x) {
			return -1;
		}

		if (y > tableVertex.y + TABLE_WIDTH || y < tableVertex.y) {
			return -1;
		}

		int i, j;
		for (i = tableSize - 1; i >= 0; --i) {
			if (x > boitesPos[i][0].x) {
				break;
			}
		}
		if (i < 0) {
			return -1;
		}

		for (j = 0; j < tableSize; ++j) {
			if (y > boitesPos[0][j].y) {
				break;
			}
		}
		if (j == tableSize) {
			return -1;
		}

		return table[i][j];
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

		public Tuile init(int posi, int posj) {
			return init(posx, posy, 0);
		}

		public Tuile init(int posi, int posj, int chiffre) {
			this.chiffre = chiffre;

			this.posx = boitesPos[posi][posj].x;
			this.posy = boitesPos[posi][posj].y;

			this.nextx = 0;
			this.nexty = 0;

			this.direction = Direction.HOLD;

			return this;
		}

		public Tuile move(Direction direction, int nexti, int nextj) {

			this.direction = direction;
			this.nextx = boitesPos[nexti][nextj].x;
			this.nexty = boitesPos[nexti][nextj].y;

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
				posy -= MiniGame.ANIME_SPEED;
				if (posy < nexty) {
					posy = nexty;
					direction = Direction.HOLD;
				}
			} else if (direction == Direction.UP) {
				posy += MiniGame.ANIME_SPEED;
				if (posy > nexty) {
					posy = nexty;
					direction = Direction.HOLD;
				}
			}

			store.draw(batch, chiffre, posx, posy, boiteWidth);

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
