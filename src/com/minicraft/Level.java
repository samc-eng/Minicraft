package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class Level {
	private int width;
	private int height;
	private Image[] tiles= new Image[256];
	private int[][] floor;
	private int[][] blocks;
	private List<Item> items= new ArrayList<>();
	
	public Level(int width, int height) {
		try {
			tiles[0]= new Image("file:resources/grass.png");
			tiles[1]= new Image("file:resources/stone.png");
			tiles[2]= new Image("file:resources/rock.png");
			tiles[3]= new Image("file:resources/tree.png");
		} catch (Exception e) {
			System.out.println("Erreur : impossible de charger une texture !");
		}
		
		this.width=width;
		this.height=height;
		this.floor = new int[width][height];
		this.blocks = new int[width][height];
		
		//des pierres posées aléatoirement
		for (int i=0; i<width; i++) {
			for(int j=0; j<height; j++) {
				if (Math.random()<0.05) {
					floor[i][j]=1;
					if (Math.random()<0.7) {
						blocks[i][j]=1;
					}
					
				} else if (Math.random()<0.02) {
					floor[i][j]=0;
					blocks[i][j]=3;
			    }else {
					floor[i][j]=0;
					blocks[i][j]=0;
				}
			}
		}
	}
	
	public void render(GraphicsContext gc, double camX, double camY, double renderWidth, double renderHeight) {
		//on dessine seulement un peu plus que la caméra
	    int xStart = (int) (camX / Config.blockSize);
	    int yStart = (int) (camY / Config.blockSize);
	    
	    int xEnd = xStart + (int)(renderWidth / Config.blockSize) + 2;
	    int yEnd = yStart + (int)(renderHeight / Config.blockSize) + 2;

	    if (xStart < 0) {xStart = 0;}
	    if (yStart < 0) {yStart = 0;}
	    if (xEnd > width) {xEnd = width;}
	    if (yEnd > height) {yEnd = height;}

	    for (int i = xStart; i < xEnd; i++) {
	        for (int j = yStart; j < yEnd; j++) {
	            int floorID=floor[i][j];
	            int blockID=blocks[i][j];
	            
	            //on dessine d'abord le sol
	            renderTile(gc, floorID, i , j);
	            
	            //puis on dessine le potentil blok sur le sol
	            if (blockID!=0) {
	            	if (blockID==1) {
	    	            renderTile(gc, 2, i , j);
	            	} else if (blockID==3){
	    	            renderTile(gc, 0, i , j);
	            	}	
		            renderTile(gc, blockID, i , j);
	            }

	        }
	    }
	    
	    for (Item item : items) {
	    	item.render(gc);
	    }
	}
	
	//fonction pour dessiner un block a partir d'une image
	private void renderTile(GraphicsContext gc, int id, int x, int y) {
	    if (id >= 0 && id < tiles.length && tiles[id] != null) {
	        gc.drawImage(tiles[id], 
	            0, 0, 16, 16,
	            x * Config.blockSize, y * Config.blockSize, 
	            Config.blockSize, Config.blockSize
	        );
	    }
	}
	
	//coordonnées du joueur en block et non pas en pixel
	public int getBlocks(double x, double y) {
		int tx=(int)(x/Config.blockSize);
		int ty=(int)(y/Config.blockSize);
		
		if (tx < 0 || ty < 0 || tx >= width || ty >= height) return 1;

	    return blocks[tx][ty];	
	}
	
	//on détruit ou pose un block
	public void setBlocks(double x, double y, int type){
		int tx = (int)(x/Config.blockSize);
		int ty = (int)(y/Config.blockSize);
		
		if (tx>=0 && ty>=0 && tx<width && ty<height) {
			if (blocks[tx][ty]==1 && type==0) { //mode destruction
				blocks[tx][ty]=0;
				//drop du block aleatoire autour de sa position
				double randomX=(Math.random()-0.5);
				double randomY=(Math.random()-0.5);
				dropItem((tx+1.0/2+randomX)*Config.blockSize, (ty+1.0/2+randomY)*Config.blockSize, 1);
			} else if (blocks[tx][ty]==0 && type != 0) { //mode construction
				blocks[tx][ty]=type;
			}
		}
	}
	
	//on actualise chaque item qui flotte
	public void updateItems(Player player) {
		for (Item item : items) {
			item.tick(player);
		}
		//on supprime l'item visuellement s'il est removed
		items.removeIf(item->item.isRemoved());
	}
	
	public int getWidth() {return this.width;}
	public int getHeight() {return this.height;}
	public void dropItem(double x, double y, int type) {
		items.add(new Item(x,y,type));
	}
	public List<Item> getItems(){return this.items;}
}






