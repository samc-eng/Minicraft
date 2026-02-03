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
		this.vitesse=0.1;
	}
	
	public void tick(Level level, InputHandler input) {
		double futurX=x;
		double futurY=y;
		
		if (input.isPressed(KeyCode.Z)) {futurY-=vitesse;dir=1;}
		if (input.isPressed(KeyCode.S)) {futurY+=vitesse;dir=0;}
		if (input.isPressed(KeyCode.D)) {futurX+=vitesse;dir=3;}
		if (input.isPressed(KeyCode.Q)) {futurX-=vitesse;dir=2;}
		
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
			this.interact(level,0);
		}
		
		if (input.isClicked(KeyCode.F)) {
			this.interact(level,1);
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
		//on crée les positions Blocks
		int xBlock=(int)((x+8)/16);
		int yBlock=(int)((y+8)/16);
		int cibleX= xBlock;
		int cibleY= yBlock;
		
		if (dir==0) {cibleY++;}
		if (dir==1) {cibleY--;}
		if (dir==3) {cibleX++;}
		if (dir==2) {cibleX--;}
		
		gc.setFill(Color.RED);
		gc.fillRect(x, y, 16, 16);
		//on dessine un carré de sélection 
		gc.setStroke(Color.YELLOW);
		gc.setLineWidth(1);
		gc.strokeRect(cibleX * 16, cibleY * 16, 16, 16);
	
		
		if (attackTimer>0) {
			gc.setFill(Color.WHITE);
			if (dir == 0) gc.fillRect(x + 2, y + 14, 12, 4);
		    if (dir == 1) gc.fillRect(x + 2, y - 6, 12, 4);
		    if (dir == 2) gc.fillRect(x - 6, y + 2, 4, 12);
		    if (dir == 3) gc.fillRect(x + 14, y + 2, 4, 12);
		}
		
	}
	
	public void interact(Level level, int type) {
		//type=0: detruire, type=1:poser block de pierre
		this.attackTimer=10;
		
		int xBlock=(int)((x+8)/16);
		int yBlock=(int)((y+8)/16);
		int cibleX= xBlock;
		int cibleY= yBlock;
		
		if (dir==0) {cibleY++;}
		if (dir==1) {cibleY--;}
		if (dir==3) {cibleX++;}
		if (dir==2) {cibleX--;}
		
		//hitbox du perso
		double pLeft = x;
		double pRight = x + 16; 
		double pTop = y;
		double pBottom = y + 16;

		//hitbox du futur mur
		double bLeft = cibleX * 16;
		double bRight = bLeft + 16;
		double bTop = cibleY * 16;
		double bBottom = bTop + 16;

		
		boolean seTouchent = !(pLeft >= bRight || pRight <= bLeft || 
		                       pTop >= bBottom || pBottom <= bTop);
		
		int cibleBlock = level.getBlocks(cibleX*16, cibleY*16);
		
		if (type != 0) {
		    //MODE CONSTRUCTION
		    if (inventory.has(type, 1) && !seTouchent) {
		        inventory.remove(type, 1);
		        //setBlock travaille avec les pixels et pas les blocks
		        level.setBlocks(cibleX*16, cibleY*16, type); 
		        System.out.println("Bloc posé !");
		    }
		} else {
		    //MODE DESTRUCTION (type == 0)
		    if (cibleBlock != 0) {
		         level.setBlocks(cibleX*16, cibleY*16, 0);
		    }
		}
	}
	
	public double getX() {return this.x;}
	public double getY() {return this.y;}
	public Inventory getInventory() {return this.inventory;}
}
