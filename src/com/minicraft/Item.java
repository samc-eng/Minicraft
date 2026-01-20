package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Item {
	private double x;
	private double y;
	private int type;
	private boolean removed = false;
	
	public Item(double x, double y, int type) {
		this.x=x;
		this.y=y;
		this.type=type;
	}
	
	public void render(GraphicsContext gc) {
        double floatingOffset = Math.sin(System.currentTimeMillis() * 0.005) * 4; 

		if (type==1) {
			gc.setFill(Color.LIGHTGRAY);
		}
		
		gc.fillRect(x+4,  y+4+floatingOffset, 8, 8);
		
		gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x + 4, y + 4 + floatingOffset, 8, 8);
    }
	
	//effet ramassage d'un item qui flotte
	public void tick(Player player) {
		double dx= player.getX()-this.x;
		double dy=player.getY()-this.y;
		double distance = Math.sqrt(dx*dx+dy*dy);
		
		double attractionSpeed=0.5;
		if (distance<20) {
			this.x+=dx/distance*attractionSpeed;
			this.y+=dy/distance*attractionSpeed;
		}
	}

    public boolean isRemoved() { return removed; }
    public void remove() { this.removed = true; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getType() { return this.type;}
}
