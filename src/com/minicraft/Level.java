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
	// --- NOUVEAU : Liste des Bots ---
	private List<Bot> bots = new ArrayList<>();

	public Level(int width, int height) {
		try {
			tiles[0] = new Image("file:resources/grass.png");
			tiles[1] = new Image("file:resources/stone.png");
			tiles[2] = new Image("file:resources/rock.png");
			tiles[3] = new Image("file:resources/tree.png");
		} catch (Exception e) {
			System.out.println("Erreur : impossible de charger une texture !");
		}

		this.width = width;
		this.height = height;
		this.floor = new int[width][height];
		this.blocks = new int[width][height];

		for (int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				floor[i][j] = 0;
				blocks[i][j] = 0;

				double rand = Math.random();
				if (rand < 0.05) {
					blocks[i][j] = 1; // Roche
				} else if (rand < 0.07) {
					blocks[i][j] = 3; // Arbre
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
				// 1. Sol (Herbe)
				renderTile(gc, floor[i][j], i, j);

				// 2. Blocs
				int blockID = blocks[i][j];
				if (blockID != 0) {
					int textureID;
					switch (blockID) {
						case 1:  textureID = 2; break; // Roche logique -> Texture Roche
						case 2:  textureID = 2; break; // Sécurité : Roche visuelle -> Texture Roche
						case 3:  textureID = 3; break; // Arbre
						default: textureID = blockID;
					}
					renderTile(gc, textureID, i, j);
				}
			}
		}

		for (Item item : items) item.render(gc);
		for (Bot bot : bots) bot.render(gc);
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

	public void setBlocks(double x, double y, int type){
		int tx = (int)(x/Config.blockSize);
		int ty = (int)(y/Config.blockSize);

		if (tx>=0 && ty>=0 && tx<width && ty<height) {
			if (blocks[tx][ty]!=0 && type==0) {
				int oldBlockID=blocks[tx][ty];
				blocks[tx][ty]=0;

				ItemDefinition modele = ItemRegistry.get(oldBlockID);
				if (modele!=null) {
					ItemStack dropStack= new ItemStack(oldBlockID, 1);
					double randomX=(Math.random()-0.5);
					double randomY=(Math.random()-0.5);
					dropItem((tx+0.5+randomX)*Config.blockSize, (ty+0.5+randomY)*Config.blockSize, dropStack);
				}
			} else if (blocks[tx][ty]==0 && type != 0) {
				blocks[tx][ty]=type;
			}
		}
	}

	// --- MISE À JOUR : Items et Bots ---
	public void updateEntities(Player player) {
		// Update items
		for (Item item : items) {
			item.tick(player);
		}
		items.removeIf(item -> item.isRemoved());

		// Update bots
		for (Bot bot : bots) {
			bot.tick(this, player);
		}
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

	// --- NOUVEAU : Méthodes pour les Bots ---
	public void addBot(double x, double y) {
		Bot nouveauBot = new Bot(x, y);
		this.bots.add(nouveauBot); // C'est cette ligne qui permet au SaveManager de les voir !
		System.out.println("BOT ENREGISTRÉ : La liste contient maintenant " + bots.size() + " bots.");
	}

	public List<Bot> getBots() {
		return this.bots;
	}

	// Pour vider les bots lors d'un nouveau chargement
	public void clearBots() {
		this.bots.clear();
	}

	// Getters et Setters pour la sauvegarde
	public int[][] getFloorArray() { return this.floor; }
	public int[][] getBlocksArray() { return this.blocks; }
	public void setFloorArray(int[][] loadedFloor) { this.floor = loadedFloor; }
	public void setBlocksArray(int[][] loadedBlocks) { this.blocks = loadedBlocks; }
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }
	public List<Item> getItems() { return this.items; }
}