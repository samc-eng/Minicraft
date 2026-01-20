package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Player {
	private double x;
	private double y;
	private double vitesse;
	private int dir;
	private int attackTimer;
	private Inventory inventory = new Inventory();
	
	public boolean up;
	public boolean down;
	public boolean right;
	public boolean left;
	
	public Player(double startX, double startY) {
		this.x=startX;
		this.y=startY;
		this.vitesse=0.2;
	}
	
	public void tick(Level level, InputHandler input) {
		double futurX=x;
		double futurY=y;
		
		if (input.isPressed(KeyCode.Z) || input.isPressed(KeyCode.UP)) {futurY-=vitesse;dir=1;}
		if (input.isPressed(KeyCode.S) || input.isPressed(KeyCode.DOWN)) {futurY+=vitesse;dir=0;}
		if (input.isPressed(KeyCode.D) || input.isPressed(KeyCode.RIGHT)) {futurX+=vitesse;dir=3;}
		if (input.isPressed(KeyCode.Q) || input.isPressed(KeyCode.LEFT)) {futurX-=vitesse;dir=2;}
		
		boolean bloque= (level.getBlocks(futurX,futurY)==1 ||
				level.getBlocks(futurX+15,futurY)==1 ||
				level.getBlocks(futurX,futurY+15)==1 ||
				level.getBlocks(futurX+15,futurY+15)==1);
		if (! bloque) {
			x=futurX;
			y=futurY;
		}
		
		if (x < 0) {x = 0;}
		if (y < 0) {y = 0;}
		if (x > level.getWidth() * 16 - 16) {x = level.getWidth() * 16 - 16;}
		if (y > level.getHeight() * 16 - 16) {y = level.getHeight() * 16 - 16;};
		
		if (attackTimer>0) {attackTimer--;}
			
		if (input.isClicked(KeyCode.SPACE)) {
			this.interact(level);
		}
		
		for (Item item : level.getItems()) {
			double dx = (x + 6) - (item.getX() + 8);
	        double dy = (y + 6) - (item.getY() + 8);
	        double distance = Math.sqrt(dx * dx + dy * dy);

	        if (distance < 12) { 
	        	inventory.add(item.getType(), 1);
	            item.remove();        
	        }
		}		
	}
	
	public void render(GraphicsContext gc) {
		gc.setFill(Color.RED);
		gc.fillRect(x, y, 16, 16);
		
		if (attackTimer>0) {
			gc.setFill(Color.WHITE);
			if (dir == 0) gc.fillRect(x + 2, y + 14, 12, 4);
		    if (dir == 1) gc.fillRect(x + 2, y - 6, 12, 4);
		    if (dir == 2) gc.fillRect(x - 6, y + 2, 4, 12);
		    if (dir == 3) gc.fillRect(x + 14, y + 2, 4, 12);
		}
	}
	
	public void interact(Level level) {
		this.attackTimer=10;
		double cibleX= x+8.0;
		double cibleY= y+8.0;
		
		if (dir==0) {cibleY+=10;}
		if (dir==1) {cibleY-=10;}
		if (dir==3) {cibleX+=10;}
		if (dir==2) {cibleX-=10;}

		level.setBlocks(cibleX,cibleY,0);	
	}
	
	public double getX() {return this.x;}
	public double getY() {return this.y;}
	public Inventory getInventory() {return this.inventory;}
}
