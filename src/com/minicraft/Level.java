package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;

public class Level {
	private int width;
	private int height;
	private Image[] tiles = new Image[256];
	private int[][] floor;
	private int[][] blocks;
	private List<Item> items = new ArrayList<>();

	public Level(int width, int height) {
		try {
			tiles[0] = TextureManager.getTexture("blocks/grass.png");
			tiles[1] = TextureManager.getTexture("blocks/stone.png"); 
			tiles[2] = TextureManager.getTexture("blocks/rock.png"); 
			tiles[3] = TextureManager.getTexture("blocks/tree.png"); 
		} catch (Exception e) {
			System.out.println("Erreur : impossible de charger une texture !");
		}

		this.width = width;
		this.height = height;
		this.floor = new int[width][height];
		this.blocks = new int[width][height];

		// --- GÉNÉRATION DU MONDE CORRIGÉE ---
		for (int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				// Par défaut, le sol est de l'herbe (ID 0)
				floor[i][j] = 0;
				blocks[i][j] = 0;

				double rand = Math.random();
				if (rand < 0.05) {
					// On place une ROCHE (ID 2 pour le visuel, mais ID 1 dans le tableau blocks pour la logique)
					blocks[i][j] = 1;
				} else if (rand < 0.07) { // 0.02 de chance (0.05 + 0.02)
					// On place un ARBRE (ID 3)
					blocks[i][j] = 3;
				}
			}
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
		if (id >= 0 && id < tiles.length && tiles[id] != null) {
			gc.drawImage(tiles[id],
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
                    if (blocks[i][j] != 0) { 
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
	

	// Getters et Setters pour la sauvegarde
	public int[][] getFloorArray() { return this.floor; }
	public int[][] getBlocksArray() { return this.blocks; }
	public void setFloorArray(int[][] loadedFloor) { this.floor = loadedFloor; }
	public void setBlocksArray(int[][] loadedBlocks) { this.blocks = loadedBlocks; }
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }
	public List<Item> getItems() { return this.items;}
}