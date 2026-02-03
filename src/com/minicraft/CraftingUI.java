package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;
import java.util.ArrayList;
import java.util.List;

public class CraftingUI {
	private List<Recipe> listeCraft = new ArrayList<>();
	private int selected=1;
	
	public CraftingUI() {
		
	}
	
	public void addRecipe(Recipe recipe) {
		this.listeCraft.add(recipe);
	}
	
	public void tick(InputHandler input, Inventory inventaire) {
		if (input.isClicked(KeyCode.T)||input.isClicked(KeyCode.DOWN)) {
			selected+=1;
			if (selected>listeCraft.size()) {selected=0;}
		}
		if (input.isClicked(KeyCode.G)||input.isClicked(KeyCode.UP)) {
			selected-=1;
			if (selected<0) {selected=listeCraft.size()-1;}
		}
		
		if (input.isClicked(KeyCode.ENTER)||input.isClicked(KeyCode.TAB)) {
			listeCraft.get(selected).Craft(inventaire);
		}	
	}
	
	public void render (GraphicsContext gc, Inventory inventaire) {
		gc.setFill(Color.BROWN);
		gc.fillRoundRect(50, 50, 200, 200, 10, 10);
        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(50, 50, 200, 200, 10, 10);
        
        gc.setFill(Color.WHITE);
        gc.fillText("CRAFTING", 110, 70);
		
		for (int i=0; i<listeCraft.size();i++) {
			Recipe recette=listeCraft.get(i);
			if (recette.canCraft(inventaire)) {
				gc.setFill(Color.LIGHTGREEN);
			} else {
				gc.setFill(Color.INDIANRED);
			}
			
			String prefix = (i == selected) ? "> " : "  ";
            gc.fillText(prefix + recette.getName(), 70, 100 + i*30);
		}
	}
}
