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
	
	public void render(GraphicsContext gc, double camX, double camY) {
	    int xStart = (int) (camX / 16);
	    int yStart = (int) (camY / 16);
	    int xEnd = xStart + (400 / 16) + 2;
	    int yEnd = yStart + (400 / 16) + 2;

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
	            gc.fillRect(i * 16, j * 16, 16, 16);


	            if (blocks[i][j] == 1) {
	                gc.setFill(Color.DARKGRAY);
	                gc.fillRect(i * 16, j * 16 - 4, 16, 12);
	                gc.setFill(Color.BLACK);
	                gc.fillRect(i * 16, j * 16 + 8, 16, 4);
	            }
	        }
	    }
	    
	    for (Item item : items) {
	    	item.render(gc);
	    }
	}
	
	public int getBlocks(double x, double y) {
		int tx=(int)(x/16);
		int ty=(int)(y/16);
		
		if (tx < 0 || ty < 0 || tx >= width || ty >= height) return 1;

	    return blocks[tx][ty];	
	}
	
	public void setBlocks(double x, double y, int type){
		int tx = (int)(x/16);
		int ty = (int)(y/16);
		
		if (tx>=0 && ty>=0 && tx<width && ty<height) {
			if (blocks[tx][ty]==1 && type==0) {
				blocks[tx][ty]=type;
				dropItem(tx*16, ty*16, 1);
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






