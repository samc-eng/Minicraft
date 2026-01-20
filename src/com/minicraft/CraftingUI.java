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
		if (input.isClicked(KeyCode.S)||input.isClicked(KeyCode.DOWN)) {
			selected+=1;
			if (selected>listeCraft.size()) {selected=0;}
		}
		if (input.isClicked(KeyCode.Z)||input.isClicked(KeyCode.UP)) {
			selected-=1;
			if (selected<0) {selected=listeCraft.size();}
		}
		
		if (input.isClicked(KeyCode.ENTER)||input.isClicked(KeyCode.TAB)) {
			listeCraft.get(selected).Craft(inventaire);
		}	
	}
	
	public void render (GraphicsContext gc, Inventory inventaire) {
		gc.setFill(Color.BROWN);
		gc.fillRect(40, 40, 40, 40);
		
		for (int i=0; i<listeCraft.size();i++) {
			Recipe recette=listeCraft.get(i);<>
			if (recette.canCraft(inventaire)) {
				//vert
			} else {
				//rouge
			}
		}
		
		//dessine le n
	}
}
