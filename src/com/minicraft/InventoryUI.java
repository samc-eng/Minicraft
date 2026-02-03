package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class InventoryUI {
	public InventoryUI() {
		
	}
	
	
	public void render(GraphicsContext pinceau, Player player) {
		pinceau.setFill(Color.color(0, 0, 0, 0.8)); 
	    pinceau.fillRoundRect(50, 50, 300, 300, 20, 20);

	    pinceau.setStroke(Color.WHITE);
	    pinceau.setLineWidth(2);
	    pinceau.strokeRoundRect(50, 50, 300, 300, 20, 20);

	    pinceau.setFill(Color.WHITE); 
	    pinceau.fillText("INVENTAIRE", 160, 80);

	    pinceau.fillText("Roche : " + player.getInventory().getAmount(1), 80, 120);
	    pinceau.fillText("Mur   : " + player.getInventory().getAmount(3), 80, 150);
	}
}
