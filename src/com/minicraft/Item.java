package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Item {
	//position de l'item
	private double x;
	private double y;
	//le type d'item ie identifie quel item
	private int type;
	private boolean removed = false;
	
	public Item(double x, double y, int type) {
		this.x=x;
		this.y=y;
		this.type=type;
	}
	
	public void render(GraphicsContext gc) {
        double floatingOffset = Math.sin(System.currentTimeMillis() * 0.005) * 4; //item flotte sur le sol 
		
		
		if (type==1) {
			gc.setFill(Color.LIGHTGRAY);
		}

		gc.fillRect(x,  y+floatingOffset, Config.itemSize, Config.itemSize);
		
		gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.5);
        gc.strokeRect(x, y + floatingOffset, Config.itemSize, Config.itemSize);
    }
	
	//effet ramassage d'un item qui flotte
	public void tick(Player player) {
		double dx=player.getCenterX()-this.getCenterX();
		double dy=player.getCenterY()-this.getCenterY();
		double distance = Math.sqrt(dx*dx+dy*dy);
		
		double attractionSpeed=0.01;
		if (distance<16) {
			this.x+=dx*attractionSpeed;
			this.y+=dy*attractionSpeed;
		}
	}

    public boolean isRemoved() { return removed; }
    public void remove() { this.removed = true; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getCenterX() { return x+Config.itemSize/2; }
    public double getCenterY() { return y+Config.itemSize/2; }
    public int getType() { return this.type;}
}
