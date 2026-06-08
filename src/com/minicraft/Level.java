package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Level {
	private int width;
	private int height;
	private int[][] floor;
	private int[][] blocks;
	private List<Item> items = new ArrayList<>();
	private List<Bot> bots = new ArrayList<>();
	private List<ArcherBot> archerBots = new ArrayList<>();
	private List<SheepBot> sheepBots = new ArrayList<>();
	private List<Arrow> arrows = new ArrayList<>();
	private int depth;
	private long seed;
	private Random spawnRandom;
	private List<int[]> portals = new ArrayList<>();

	public Level(int width, int height) {
		this(width, height, 0, new Random().nextLong());
	}

	public Level(int width, int height, int depth, long seed) {
		this.width  = width;
		this.height = height;
		this.depth  = depth;
		this.seed   = seed;
		this.spawnRandom = new Random(seed + depth * 97L + 13L);
		this.floor  = new int[width][height];
		this.blocks = new int[width][height];

		MapGenerator generator = new MapGenerator();

		if (depth == 0) {
			generator.fillWithOcean(width, height, floor);
			generator.generateIslands(width, height, floor, seed);
			generator.generateForests(width, height, floor, blocks, seed);
			generator.generateRocks(width, height, floor, blocks, seed);
			generator.placeCaveEntrances(width, height, floor, blocks, portals, seed);
		} else {
			generator.generateCave(width, height, floor, blocks, portals, seed, depth);
			generator.placeOres(width, height, blocks, seed, depth);
		}
	}

	public void render(GraphicsContext gc, double camX, double camY, double renderWidth, double renderHeight) {
		int xStart = (int) (camX / Config.blockSize);
		int yStart = (int) (camY / Config.blockSize);
		int xEnd = xStart + (int)(renderWidth / Config.blockSize) + 2;
		int yEnd = yStart + (int)(renderHeight / Config.blockSize) + 2;

		if (xStart < 0) xStart = 0;
		if (yStart < 0) yStart = 0;
		if (xEnd > width) xEnd = width;
		if (yEnd > height) yEnd = height;

		for (int i = xStart; i < xEnd; i++) {
			for (int j = yStart; j < yEnd; j++) {
				renderTile(gc, floor[i][j], i, j);

				int blockID = blocks[i][j];
				if (blockID != 0) {
					int textureID;
					switch (blockID) {
						case 1:  textureID = 2; break;
						case 2:  textureID = 2; break;
						case 3:  textureID = 3; break;
						default: textureID = blockID;
					}
					renderTile(gc, textureID, i, j);
				}
			}
		}

		for (Item item : items) item.render(gc);
		for (SheepBot sheep : sheepBots) sheep.render(gc);
		for (Bot bot : bots) bot.render(gc);
		for (ArcherBot archer : archerBots) archer.render(gc);
		for (Arrow arrow : arrows) arrow.render(gc);

		double pulse = (Math.sin(System.currentTimeMillis() / 300.0) + 1.0) / 2.0;
		double alpha = 0.25 + pulse * 0.45;
		double expand = pulse * Config.blockSize * 0.6;
		gc.setStroke(Color.color(0.7, 0.1, 1.0, alpha));
		gc.setLineWidth(2.5);
		for (int[] portal : portals) {
			double px = portal[0] * Config.blockSize - expand / 2;
			double py = portal[1] * Config.blockSize - expand / 2;
			double sz = Config.blockSize + expand;
			gc.strokeRect(px, py, sz, sz);
			gc.setFill(Color.color(0.7, 0.1, 1.0, alpha * 0.3));
			gc.fillRect(px, py, sz, sz);
		}
	}

	private void renderTile(GraphicsContext gc, int id, int x, int y) {
		ItemDefinition def = ItemRegistry.get(id);
		double px = x * Config.blockSize;
		double py = y * Config.blockSize;
		double sz = Config.blockSize;

		if (def != null && def.texture != null && !def.texture.isError()) {
			gc.drawImage(def.texture, 0, 0, 16, 16, px, py, sz, sz);
		} else {
			Color fallback;
			if      (id == MapGenerator.BLOCK_CAVE_ENTRANCE) fallback = Color.PURPLE;
			else if (id == MapGenerator.BLOCK_OBSIDIAN)      fallback = Color.color(0.15, 0.0, 0.25);
			else if (id == MapGenerator.FLOOR_GRASS)         fallback = Color.color(0.3, 0.65, 0.2);
			else if (id == MapGenerator.FLOOR_WATER)         fallback = Color.color(0.15, 0.4, 0.8);
			else if (id == MapGenerator.FLOOR_SAND)          fallback = Color.color(0.85, 0.75, 0.4);
			else if (id == MapGenerator.FLOOR_STONE)         fallback = Color.GRAY;
			else if (id == MapGenerator.BLOCK_ROCK)          fallback = Color.DARKGRAY;
			else if (id == MapGenerator.BLOCK_TREE)          fallback = Color.DARKGREEN;
			else                                             fallback = Color.MAGENTA;
			gc.setFill(fallback);
			gc.fillRect(px, py, sz, sz);
		}
	}

	public int getBlocks(double x, double y) {
		int tx = (int)(x / Config.blockSize);
		int ty = (int)(y / Config.blockSize);
		if (tx < 0 || ty < 0 || tx >= width || ty >= height) return 1;
		return blocks[tx][ty];
	}

	public void setBlocks(double x, double y, int type){
		int tx = (int)(x / Config.blockSize);
		int ty = (int)(y / Config.blockSize);

		if (tx >= 0 && ty >= 0 && tx < width && ty < height) {
			if (type == 0 && blocks[tx][ty] != 0) {
				int oldBlockId = blocks[tx][ty];
				blocks[tx][ty] = 0;

				double dropX = (tx + 0.5 + (Math.random() - 0.5)) * Config.blockSize;
				double dropY = (ty + 0.5 + (Math.random() - 0.5)) * Config.blockSize;

				for (Drop drop : DropTable.get(oldBlockId)) {
					if (drop.rolls()) {
						ItemStack stack = new ItemStack(drop.itemId, drop.rollAmount());
						dropItem(dropX, dropY, stack);
					}
				}
			} else if (blocks[tx][ty] == 0 && type != 0) {
				blocks[tx][ty] = type;
			}
		}
	}

	public void updateEntities(Player player) {
		for (Item item : items) {
			item.tick(player);
		}
		items.removeIf(item -> item.isRemoved());

		for (SheepBot sheep : sheepBots) {
			sheep.tick(this);
		}

		for (Bot bot : bots) {
			bot.tick(this, player);
		}

		for (ArcherBot archer : archerBots) {
			Arrow arrow = archer.tick(this, player);
			if (arrow != null) {
				arrows.add(arrow);
			}
		}

		for (Arrow arrow : arrows) {
			arrow.tick(this, player);
		}
		arrows.removeIf(arrow -> arrow.isRemoved());
	}

	public void dropItem(double x, double y, ItemStack stack) {
		ItemDefinition modele = stack.getDefinition();
		if (modele == null) return;

		Item entiteAuSol;
		if (modele.placeable) {
			entiteAuSol = new PlaceableItem(x, y, stack, stack.getItemId());
		} else {
			entiteAuSol = new ResourceItem(x, y, stack);
		}
		items.add(entiteAuSol);
	}

	public void addBot(double x, double y) {
		bots.add(new Bot(x, y));
	}

	public void addArcherBot(double x, double y) {
		archerBots.add(new ArcherBot(x, y));
	}

	public void addSheepBot(double x, double y) {
		sheepBots.add(new SheepBot(x, y));
	}

	public List<Bot> getBots() {
		return bots;
	}

	public List<ArcherBot> getArcherBots() {
		return archerBots;
	}

	public List<SheepBot> getSheepBots() {
		return sheepBots;
	}

	public void clearBots() {
		bots.clear();
	}

	public void clearArcherBots() {
		archerBots.clear();
	}

	public void clearArrows() {
		arrows.clear();
	}

	public void clearSheepBots() {
		sheepBots.clear();
	}

	public void clearEnemies() {
		clearBots();
		clearArcherBots();
		clearArrows();
	}

	public double[] getSafeSpawn(){
		for (int attempt = 0; attempt < Config.ENEMY_SPAWN_ATTEMPTS * 4; attempt++) {
			int tx = spawnRandom.nextInt(Math.max(1, width - 4)) + 2;
			int ty = spawnRandom.nextInt(Math.max(1, height - 4)) + 2;
			if (isSafeSpawnTile(tx, ty)) {
				return new double[]{tx * Config.blockSize, ty * Config.blockSize};
			}
		}

		for (int tx = 2; tx < width - 2; tx++) {
			for (int ty = 2; ty < height - 2; ty++) {
				if (isSafeSpawnTile(tx, ty)) {
					return new double[]{tx * Config.blockSize, ty * Config.blockSize};
				}
			}
		}

		return new double[]{Config.blockSize, Config.blockSize};
	}

	public double[] findEnemySpawnAround(double anchorCenterX, double anchorCenterY,
			List<double[]> reservedPositions) {
		for (int attempt = 0; attempt < Config.ENEMY_SPAWN_ATTEMPTS; attempt++) {
			double angle = spawnRandom.nextDouble() * Math.PI * 2.0;
			double distance = Config.ENEMY_MIN_PLAYER_SPAWN_DISTANCE
					+ spawnRandom.nextDouble()
					* (Config.ENEMY_MAX_PLAYER_SPAWN_DISTANCE - Config.ENEMY_MIN_PLAYER_SPAWN_DISTANCE);
			double candidateX = anchorCenterX + Math.cos(angle) * distance - Config.blockSize / 2.0;
			double candidateY = anchorCenterY + Math.sin(angle) * distance - Config.blockSize / 2.0;

			if (isValidEnemySpawn(candidateX, candidateY, anchorCenterX, anchorCenterY, reservedPositions,
					Config.ENEMY_MIN_PLAYER_SPAWN_DISTANCE, Config.ENEMY_MIN_BOT_SPAWN_DISTANCE)) {
				return new double[]{candidateX, candidateY};
			}
		}

		for (int attempt = 0; attempt < Config.ENEMY_SPAWN_ATTEMPTS; attempt++) {
			double candidateX = (spawnRandom.nextInt(Math.max(1, width - 2)) + 1) * Config.blockSize;
			double candidateY = (spawnRandom.nextInt(Math.max(1, height - 2)) + 1) * Config.blockSize;

			if (isValidEnemySpawn(candidateX, candidateY, anchorCenterX, anchorCenterY, reservedPositions,
					Config.ENEMY_MIN_PLAYER_SPAWN_DISTANCE, Config.ENEMY_MIN_BOT_SPAWN_DISTANCE)) {
				return new double[]{candidateX, candidateY};
			}
		}

		double[] fallback = getSafeSpawn();
		if (isFarEnoughFromReserved(fallback, reservedPositions, Config.ENEMY_MIN_BOT_SPAWN_DISTANCE)) {
			return fallback;
		}
		return null;
	}

	public double[] findPassiveSpawnAround(double anchorCenterX, double anchorCenterY,
			List<double[]> reservedPositions) {
		for (int attempt = 0; attempt < Config.SHEEP_SPAWN_ATTEMPTS; attempt++) {
			double angle = spawnRandom.nextDouble() * Math.PI * 2.0;
			double distance = Config.SHEEP_MIN_PLAYER_SPAWN_DISTANCE
					+ spawnRandom.nextDouble()
					* (Config.SHEEP_NEAR_PLAYER_MAX_DISTANCE - Config.SHEEP_MIN_PLAYER_SPAWN_DISTANCE);
			double candidateX = anchorCenterX + Math.cos(angle) * distance - Config.blockSize / 2.0;
			double candidateY = anchorCenterY + Math.sin(angle) * distance - Config.blockSize / 2.0;

			if (isValidPassiveSpawn(candidateX, candidateY, anchorCenterX, anchorCenterY,
					Config.SHEEP_MIN_PLAYER_SPAWN_DISTANCE, reservedPositions)) {
				return new double[]{candidateX, candidateY};
			}
		}
		return null;
	}

	public double[] findPassiveSpawnInRegion(int minTileX, int minTileY, int maxTileX, int maxTileY,
			double anchorCenterX, double anchorCenterY, List<double[]> reservedPositions) {
		minTileX = Math.max(1, minTileX);
		minTileY = Math.max(1, minTileY);
		maxTileX = Math.min(width - 2, maxTileX);
		maxTileY = Math.min(height - 2, maxTileY);

		if (minTileX > maxTileX || minTileY > maxTileY) {
			return null;
		}

		for (int attempt = 0; attempt < Config.SHEEP_SPAWN_ATTEMPTS; attempt++) {
			int tx = minTileX + spawnRandom.nextInt(maxTileX - minTileX + 1);
			int ty = minTileY + spawnRandom.nextInt(maxTileY - minTileY + 1);
			double candidateX = tx * Config.blockSize;
			double candidateY = ty * Config.blockSize;

			if (isValidPassiveSpawn(candidateX, candidateY, anchorCenterX, anchorCenterY,
					Config.SHEEP_MIN_PLAYER_SPAWN_DISTANCE, reservedPositions)) {
				return new double[]{candidateX, candidateY};
			}
		}
		return null;
	}

	public double[] findPassiveSpawnAnywhere(double anchorCenterX, double anchorCenterY,
			List<double[]> reservedPositions) {
		return findPassiveSpawnInRegion(1, 1, width - 2, height - 2,
				anchorCenterX, anchorCenterY, reservedPositions);
	}

	private boolean isSafeSpawnTile(int tx, int ty) {
		for (int i = tx - 1; i <= tx + 1; i++) {
			for (int j = ty - 1; j <= ty + 1; j++) {
				if (!isWalkableTile(i, j)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isValidEnemySpawn(double x, double y, double anchorCenterX, double anchorCenterY,
			List<double[]> reservedPositions, double minAnchorDistance, double minReservedDistance) {
		if (!isValidEntityPosition(x, y)) {
			return false;
		}

		double centerX = x + Config.blockSize / 2.0;
		double centerY = y + Config.blockSize / 2.0;
		if (distance(centerX, centerY, anchorCenterX, anchorCenterY) < minAnchorDistance) {
			return false;
		}

		return isFarEnoughFromReserved(new double[]{x, y}, reservedPositions, minReservedDistance);
	}


	public void removeDeadBots() {
        if (bots != null) {
            bots.removeIf(bot -> bot.isDead());
        }
        if (archerBots != null) {
            archerBots.removeIf(archer -> archer.isDead());
        }
		if (sheepBots != null) { 
            sheepBots.removeIf(sheep -> sheep.isDead()); 
        }
    }

	private boolean isValidPassiveSpawn(double x, double y, double anchorCenterX, double anchorCenterY,
			double minAnchorDistance, List<double[]> reservedPositions) {
		if (!isValidEntityPosition(x, y)) {
			return false;
		}

		double centerX = x + Config.blockSize / 2.0;
		double centerY = y + Config.blockSize / 2.0;
		if (distance(centerX, centerY, anchorCenterX, anchorCenterY) < minAnchorDistance) {
			return false;
		}

		return isFarEnoughFromReserved(new double[]{x, y}, reservedPositions,
				Config.SHEEP_MIN_ENTITY_SPAWN_DISTANCE);
	}


	private boolean isFarEnoughFromReserved(double[] candidate, List<double[]> reservedPositions,
			double minReservedDistance) {
		if (reservedPositions == null) {
			return true;
		}

		double centerX = candidate[0] + Config.blockSize / 2.0;
		double centerY = candidate[1] + Config.blockSize / 2.0;
		for (double[] reserved : reservedPositions) {
			double reservedCenterX = reserved[0] + Config.blockSize / 2.0;
			double reservedCenterY = reserved[1] + Config.blockSize / 2.0;
			if (distance(centerX, centerY, reservedCenterX, reservedCenterY) < minReservedDistance) {
				return false;
			}
		}
		return true;
	}

	private boolean isValidEntityPosition(double x, double y) {
		return isInsideMap(x, y)
				&& isInsideMap(x + Config.blockSize - 1, y + Config.blockSize - 1)
				&& !isAreaBlocked(x, y, Config.blockSize, Config.blockSize)
				&& !isWaterAt(x, y)
				&& !isWaterAt(x + Config.blockSize - 1, y)
				&& !isWaterAt(x, y + Config.blockSize - 1)
				&& !isWaterAt(x + Config.blockSize - 1, y + Config.blockSize - 1);
	}

	private boolean isWaterAt(double px, double py) {
		int tx = (int)(px / Config.blockSize);
		int ty = (int)(py / Config.blockSize);
		if (tx < 0 || ty < 0 || tx >= width || ty >= height) {
			return true;
		}
		return floor[tx][ty] == MapGenerator.FLOOR_WATER;
	}

	public boolean isEntityPositionValid(double x, double y) {
		return isValidEntityPosition(x, y);
	}

	private double distance(double ax, double ay, double bx, double by) {
		double dx = ax - bx;
		double dy = ay - by;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public boolean isSolid(double x, double y) {
		int b = getBlocks(x, y);
		return b >= 1 && b != MapGenerator.BLOCK_CAVE_ENTRANCE;
	}

	public boolean isWalkableTile(int tx, int ty) {
		if (tx < 0 || tx >= width || ty < 0 || ty >= height) return false;

		double centerX = tx * Config.blockSize + (Config.blockSize / 2.0);
		double centerY = ty * Config.blockSize + (Config.blockSize / 2.0);
		return floor[tx][ty] != MapGenerator.FLOOR_WATER && !isSolid(centerX, centerY);
	}

	public boolean isAreaBlocked(double x, double y, double w, double h) {
		double right = x + Math.max(0, w - 1);
		double bottom = y + Math.max(0, h - 1);
		return isSolid(x, y)
				|| isSolid(right, y)
				|| isSolid(x, bottom)
				|| isSolid(right, bottom);
	}

	public boolean isInsideMap(double px, double py) {
		double mapWidthPixels = width * Config.blockSize;
		double mapHeightPixels = height * Config.blockSize;
		return px >= 0 && px < mapWidthPixels && py >= 0 && py < mapHeightPixels;
	}

	public int[][] getFloorArray() { return floor; }
	public int[][] getBlocksArray() { return blocks; }

	public void setFloorArray(int[][] loadedFloor) {
		floor = loadedFloor;
		if (loadedFloor != null && loadedFloor.length > 0 && loadedFloor[0].length > 0) {
			width = loadedFloor.length;
			height = loadedFloor[0].length;
		}
	}

	public void setBlocksArray(int[][] loadedBlocks) {
		blocks = loadedBlocks;
		if (loadedBlocks != null && loadedBlocks.length > 0 && loadedBlocks[0].length > 0) {
			width = loadedBlocks.length;
			height = loadedBlocks[0].length;
		}
	}

	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public List<Item> getItems() { return items; }
	public int getDepth() { return depth; }
	public long getSeed() { return seed; }
	public List<int[]> getPortals() { return portals; }

	public int getTile(int tx, int ty) {
		if (tx < 0 || ty < 0 || tx >= width || ty >= height) return 0;
		return blocks[tx][ty];
	}
}
