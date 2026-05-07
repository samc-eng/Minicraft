package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;

public class Level {
	private int width;
	private int height;
	private int[][] floor;
	private int[][] blocks;
	private List<Item> items = new ArrayList<>();
	private int depth;
	private long seed;
	private List<int[]> portals = new ArrayList<>();

	// Constructeur surface (depth = 0)
	public Level(int width, int height) {
		this(width, height, 0, new java.util.Random().nextLong());
	}

	// Constructeur générique — surface (depth=0) ou souterrain (depth>=1)
	public Level(int width, int height, int depth, long seed) {
		this.width  = width;
		this.height = height;
		this.depth  = depth;
		this.seed   = seed;
		this.floor  = new int[width][height];
		this.blocks = new int[width][height];

		MapGenerator generator = new MapGenerator();

		if (depth == 0) {
			// --- Surface ---
			generator.fillWithOcean(width, height, floor);
			generator.generateIslands(width, height, floor, seed);
			generator.generateForests(width, height, floor, blocks, seed);
			generator.generateRocks(width, height, floor, blocks, seed);
			generator.placeCaveEntrances(width, height, floor, blocks, portals, seed);
		} else {
			// --- Souterrain ---
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
				// On dessine l'herbe au sol d'abord
				renderTile(gc, floor[i][j], i, j);

				// 2. On dessine le bloc par-dessus seulement s'il n'est pas vide (0)
				int blockID = blocks[i][j];
				if (blockID != 0) {
					// Si c'est une roche (ID 1 dans blocks), on dessine la texture correspondante
					renderTile(gc, blockID, i, j);
				}
			}
		}

		for (Item item : items) {
			item.render(gc);
		}
	}

	private void renderTile(GraphicsContext gc, int id, int x, int y) {
		ItemDefinition def = ItemRegistry.get(id);
		if (def != null && def.texture != null) {
			gc.drawImage(def.texture,
				0, 0, 16, 16,
				x * Config.blockSize, y * Config.blockSize,
				Config.blockSize, Config.blockSize
			);
		}
	}

	public int getBlocks(double x, double y) {
		int tx = (int)(x/Config.blockSize);
		int ty = (int)(y/Config.blockSize);
		if (tx < 0 || ty < 0 || tx >= width || ty >= height) return 1;
		return blocks[tx][ty];
	}

	//Modification du monde
	public void setBlocks(double x, double y, int type){
		int tx = (int)(x/Config.blockSize);
		int ty = (int)(y/Config.blockSize);
		
		if (tx>=0 && ty>=0 && tx<width && ty<height) {
			if (type == 0 && blocks[tx][ty] != 0) {
				int oldBlockId = blocks[tx][ty];
				blocks[tx][ty] = 0;

				double dropX = (tx + 0.5 + (Math.random() - 0.5)) * Config.blockSize;
				double dropY = (ty + 0.5 + (Math.random() - 0.5)) * Config.blockSize;

				// Pour chaque drop possible de ce bloc
				for (Drop drop : DropTable.get(oldBlockId)) {
					if (drop.rolls()) {
						// On crée un ItemStack avec la bonne quantité
						ItemStack stack = new ItemStack(drop.itemId, drop.rollAmount());
						// On le droppe au sol
						dropItem(dropX, dropY, stack);
					}
    		}
		}
			else if (blocks[tx][ty]==0 && type != 0) { //mode construction
				blocks[tx][ty]=type;
			}
		}
	}

	public void updateItems(Player player) {
		for (Item item : items) {
			item.tick(player);
		}
		items.removeIf(item -> item.isRemoved());
	}

	public void dropItem(double x, double y, ItemStack stack) {
	    ItemDefinition modele = stack.getDefinition();
	    if (modele == null) { return; }
	    
	    Item entiteAuSol;
	    if (modele.placeable) {
	        entiteAuSol = new PlaceableItem(x, y, stack, stack.getItemId());
	    } else {
	        entiteAuSol = new ResourceItem(x, y, stack);
	    }
	    items.add(entiteAuSol);
	}

	//on trouve un point sûr pour apparaitre
	public double[] getSafeSpawn(){
		boolean isSafe=false;
		int tx = 50;
		int ty = 50;

		while (! isSafe){
			tx = (int) (Math.random() * (width - 4)) + 2;
            ty = (int) (Math.random() * (height - 4)) + 2;

			isSafe = true; 

            //on scanne un carré de 3x3 autour de ce point
            for (int i = tx - 1; i <= tx + 1; i++) {
                for (int j = ty - 1; j <= ty + 1; j++) {
					if (blocks[i][j] != 0 || floor[i][j] == MapGenerator.FLOOR_WATER) {
                        isSafe = false; 
                        break; 
                    }
                }
                if (!isSafe) break;
            }
		}

		double pixelX = tx * Config.blockSize + (Config.blockSize / 2.0);
        double pixelY = ty * Config.blockSize + (Config.blockSize / 2.0);

        return new double[]{pixelX, pixelY};
	}
	

	/**
	 * Retourne true si la tuile bloque le mouvement.
	 * La porte d'obsidienne (entrée de grotte) est traversable — la transition
	 * est gérée par Main, pas par la collision.
	 */
	public boolean isSolid(double x, double y) {
		int b = getBlocks(x, y);
		return b >= 1 && b != MapGenerator.BLOCK_CAVE_ENTRANCE;
	}

	// Getters et Setters pour la sauvegarde
	public int[][] getFloorArray() { return this.floor; }
	public int[][] getBlocksArray() { return this.blocks; }
	public void setFloorArray(int[][] loadedFloor) { this.floor = loadedFloor; }
	public void setBlocksArray(int[][] loadedBlocks) { this.blocks = loadedBlocks; }
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }
	public List<Item> getItems() { return this.items; }
	public int getDepth() { return this.depth; }
	public long getSeed() { return this.seed; }
	public List<int[]> getPortals() { return this.portals; }
}