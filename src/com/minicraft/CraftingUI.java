package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;
import java.util.ArrayList;
import java.util.List;

/**
 * CraftingUI gère deux menus de craft :
 *  - recettes de base (accessibles n'importe où, touche C)
	 *  - recettes d'établi (nécessitent d'être près d'un Workbench, touche B)
	 */
public class CraftingUI {

    // Recettes de base (main)
    private List<Recipe> recettesBase = new ArrayList<>();
	    // Recettes d'établi (Workbench)
    private List<Recipe> recettesEtabli = new ArrayList<>();

    // Liste actuellement affichée
    private List<Recipe> listeCraft = recettesBase;

    private int selected = 0;

    // true = menu établi ouvert, false = menu de base
    private boolean modeEtabli = false;

    public CraftingUI() {}

    // Ajouter une recette de base (sans établi)
    public void addRecipe(Recipe recipe) {
		        this.recettesBase.add(recipe);
	}

    // Ajouter une recette nécessitant un établi
    public void addWorkbenchRecipe(Recipe recipe) {
		        this.recettesEtabli.add(recipe);
	}

    // Retourne si le menu établi est actif
    public boolean isModeEtabli() { return modeEtabli; }

    // Bascule entre menu de base et établi
    public void setModeEtabli(boolean mode) {
		        this.modeEtabli = mode;
		        this.listeCraft = mode ? recettesEtabli : recettesBase;
		        this.selected = 0;
	}

    public void tick(InputHandler input, Player player) { // <-- Changement ici
        Inventory inventaire = player.getInventory();
        if (listeCraft.isEmpty()) return;

        if (input.isClicked(KeyCode.T) || input.isClicked(KeyCode.DOWN)) {
            selected += 1;
            if (selected >= listeCraft.size()) { selected = 0; }
        }
        if (input.isClicked(KeyCode.G) || input.isClicked(KeyCode.UP)) {
            selected -= 1;
            if (selected < 0) { selected = listeCraft.size() - 1; }
        }

        if (input.isClicked(KeyCode.ENTER) || input.isClicked(KeyCode.TAB)) {
            // On donne le joueur ENTIER à la recette !
            listeCraft.get(selected).Craft(player); 
        }
    }

    
    public void render(GraphicsContext gc, Player player) {
        // C'EST CETTE LIGNE QUI MANQUAIT AU TOUT DÉBUT :
        Inventory inventaire = player.getInventory(); 
        
        // Fond du panneau
        gc.setFill(modeEtabli ? Color.SADDLEBROWN : Color.BROWN);
        gc.fillRoundRect(50, 50, 230, 280, 10, 10);
        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(50, 50, 230, 280, 10, 10);

        // Titre
        gc.setFill(Color.WHITE);
        String titre = modeEtabli ? "ETABLI" : "CRAFT (main)";
        gc.fillText(titre, 80, 75);

        if (listeCraft.isEmpty()) {
            gc.setFill(Color.LIGHTGRAY);
            gc.fillText("Aucune recette disponible", 65, 110);
            return;
        }

        // Liste des recettes
        for (int i = 0; i < listeCraft.size(); i++) {
            Recipe recette = listeCraft.get(i);

            if (recette.canCraft(inventaire)) {
                gc.setFill(Color.LIGHTGREEN);
            } else {
                gc.setFill(Color.INDIANRED);
            }

            String prefix = (i == selected) ? "> " : "  ";
            gc.fillText(prefix + recette.getName(), 65, 100 + i * 22);
        }

        // Légende
        gc.setFill(Color.LIGHTYELLOW);
        gc.fillText("[T/G] naviguer  [Entree] crafter", 55, 320);
    }
}
