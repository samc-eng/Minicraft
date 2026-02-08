package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class Level {
	private int width;
	private int height;
	private int[][] floor;
	private int[][] blocks;
	private List<Item> items= new ArrayList<>();
	
	public Level(int width, int height) {
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
					
				} else {
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
	            if (floor[i][j] == 0) { 
	            	gc.setFill(Color.GREEN);
	            }else {
	            	gc.setFill(Color.GRAY);
	            }
	            gc.fillRect(i * Config.blockSize, j * Config.blockSize, Config.blockSize, Config.blockSize);


	            if (blocks[i][j] == 1) {
	                gc.setFill(Color.DARKGRAY);
	                gc.fillRect(i * Config.blockSize, j * Config.blockSize - 4, Config.blockSize, 12);
	                gc.setFill(Color.BLACK);
	                gc.fillRect(i * Config.blockSize, j * Config.blockSize + 8, Config.blockSize, 4);
	            }
	        }
	    }
	    
	    for (Item item : items) {
	    	item.render(gc);
	    }
	}
	
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
			if (blocks[tx][ty]==1 && type==0) {
				blocks[tx][ty]=0;
				dropItem(tx*Config.blockSize, ty*Config.blockSize, 1);
			} else if (blocks[tx][ty]==0 && type != 0) {
				blocks[tx][ty]=type;
			}
		}
	}
	
	public void updateItems(Player player) {
		for (Item item : items) {
			item.tick(player);
		}
		items.removeIf(item->item.isRemoved());
	}
	
	public int getWidth() {return this.width;}
	public int getHeight() {return this.height;}
	public void dropItem(double x, double y, int type) {
		items.add(new Item(x,y,type));
	}
	public List<Item> getItems(){return this.items;}
}






