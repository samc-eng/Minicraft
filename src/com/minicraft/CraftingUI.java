package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// menu de craft (touche C ou B pour l'etabli)
// affiche les recettes avec icones, et change de couleur selon le mode
public class CraftingUI {

    private static final int PANEL_W      = 440;
    private static final int PADDING      = 22;
    private static final int HEADER_H     = 64;
    private static final int ROW_H        = 64;
    private static final int VISIBLE_ROWS = 6;
    private static final int ICON_BIG     = 44;
    private static final int ICON_SMALL   = 22;

    private static final Font FONT_TITLE = Font.font("Arial Rounded MT Bold", FontWeight.BOLD, 26);
    private static final Font FONT_NAME  = Font.font("Arial", FontWeight.BOLD, 16);
    private static final Font FONT_CNT   = Font.font("Arial", FontWeight.BOLD, 13);
    private static final Font FONT_HINT  = Font.font("Arial", 13);

    private final List<Recipe> recettesBase   = new ArrayList<>();
    private final List<Recipe> recettesEtabli = new ArrayList<>();
    private List<Recipe> listeCraft = recettesBase;

    private int selected = 0;
    private int scrollOffset = 0;
    private boolean modeEtabli = false;

    public CraftingUI() {}

    public void addRecipe(Recipe recipe)          { this.recettesBase.add(recipe); }
    public void addWorkbenchRecipe(Recipe recipe) { this.recettesEtabli.add(recipe); }
    public boolean isModeEtabli()                 { return modeEtabli; }

    public void setModeEtabli(boolean mode) {
        this.modeEtabli   = mode;
        this.listeCraft   = mode ? recettesEtabli : recettesBase;
        this.selected     = 0;
        this.scrollOffset = 0;
    }

    public void tick(InputHandler input, Player player) {
        if (listeCraft.isEmpty()) return;

        if (input.isClicked(KeyCode.T) || input.isClicked(KeyCode.DOWN)) {
            selected = (selected + 1) % listeCraft.size();
        }
        if (input.isClicked(KeyCode.G) || input.isClicked(KeyCode.UP)) {
            selected = (selected - 1 + listeCraft.size()) % listeCraft.size();
        }
        // on decale la liste pour que la recette selectionnee reste visible
        if (selected < scrollOffset) scrollOffset = selected;
        if (selected >= scrollOffset + VISIBLE_ROWS) scrollOffset = selected - VISIBLE_ROWS + 1;

        if (input.isClicked(KeyCode.ENTER) || input.isClicked(KeyCode.TAB)) {
            listeCraft.get(selected).Craft(player);
        }
    }

    public void render(GraphicsContext gc, Player player) {
        gc.save();
        try {
            renderInternal(gc, player);
        } finally {
            gc.restore();
        }
    }

    private void renderInternal(GraphicsContext gc, Player player) {
        double canvasW = gc.getCanvas().getWidth();
        double canvasH = gc.getCanvas().getHeight();

        int rowsShown = Math.min(VISIBLE_ROWS, Math.max(1, listeCraft.size()));
        double panelH = HEADER_H + rowsShown * ROW_H + 40; // +40 pour le bas du panneau
        double panelX = (canvasW - PANEL_W) / 2.0;
        double panelY = (canvasH - panelH) / 2.0;

        // fond noir transparent
        gc.setFill(Color.rgb(0, 0, 0, 0.55));
        gc.fillRect(0, 0, canvasW, canvasH);

        // les couleurs changent si on est dans le mode etabli ou pas
        Color bg          = modeEtabli ? Color.rgb(30, 42, 60, 0.97) : Color.rgb(50, 35, 22, 0.97);
        Color borderColor = modeEtabli ? Color.rgb(120, 180, 255)    : Color.GOLD;
        Color headerBg    = modeEtabli ? Color.rgb(45, 60, 85, 1.0)  : Color.rgb(70, 50, 30, 1.0);
        String title      = modeEtabli ? "ETABLI"                    : "CRAFT (main)";

        // la boite du menu
        gc.setFill(bg);
        gc.fillRoundRect(panelX, panelY, PANEL_W, panelH, 14, 14);
        gc.setStroke(borderColor);
        gc.setLineWidth(3);
        gc.strokeRoundRect(panelX, panelY, PANEL_W, panelH, 14, 14);

        // bandeau du haut avec le titre
        gc.setFill(headerBg);
        gc.fillRoundRect(panelX, panelY, PANEL_W, HEADER_H, 14, 14);
        gc.setStroke(borderColor);
        gc.setLineWidth(1);
        gc.strokeLine(panelX, panelY + HEADER_H, panelX + PANEL_W, panelY + HEADER_H);

        gc.setFont(FONT_TITLE);
        gc.setFill(borderColor);
        gc.fillText(title, panelX + PADDING, panelY + 40);

        if (listeCraft.isEmpty()) {
            gc.setFont(FONT_NAME);
            gc.setFill(Color.LIGHTGRAY);
            gc.fillText("Aucune recette disponible.", panelX + PADDING, panelY + HEADER_H + 30);
            return;
        }

        // on affiche les recettes une par une
        double listX = panelX;
        double listY = panelY + HEADER_H;
        int end = Math.min(listeCraft.size(), scrollOffset + VISIBLE_ROWS);
        for (int i = scrollOffset; i < end; i++) {
            Recipe r = listeCraft.get(i);
            double ry = listY + (i - scrollOffset) * ROW_H;
            drawRecipeRow(gc, r, player, listX, ry, PANEL_W, i == selected);
        }

        // ligne du bas avec les controles et la position
        gc.setFont(FONT_HINT);
        gc.setFill(Color.rgb(220, 220, 220));
        String hint = "↑/↓ naviguer    Entrée crafter    " +
                      (selected + 1) + "/" + listeCraft.size();
        gc.fillText(hint, panelX + PADDING, panelY + panelH - 14);
    }

    // dessine une ligne pour une recette
    private void drawRecipeRow(GraphicsContext gc, Recipe r, Player player,
                               double x, double y, double w, boolean isSelected) {
        boolean craftable = r.canCraft(player);

        // si c'est la recette selectionnee on met un fond colore (vert ou rouge)
        if (isSelected) {
            gc.setFill(craftable ? Color.rgb(80, 140, 70, 0.6)
                                 : Color.rgb(140, 60, 60, 0.6));
            gc.fillRect(x + 4, y + 2, w - 8, ROW_H - 4);
            gc.setStroke(craftable ? Color.LIGHTGREEN : Color.rgb(255, 120, 120));
            gc.setLineWidth(2);
            gc.strokeRect(x + 4, y + 2, w - 8, ROW_H - 4);
        }

        // icone de l'objet qu'on obtient
        double iconX = x + PADDING;
        double iconY = y + (ROW_H - ICON_BIG) / 2.0;
        ItemDefinition resDef = ItemRegistry.get(r.getResultId());
        if (resDef != null && resDef.texture != null) {
            gc.drawImage(resDef.texture, iconX, iconY, ICON_BIG, ICON_BIG);
        }
        if (r.getResultCount() > 1) {
            gc.setFont(FONT_CNT);
            gc.setFill(Color.WHITE);
            gc.fillText("x" + r.getResultCount(),
                        iconX + ICON_BIG - 18, iconY + ICON_BIG - 2);
        }

        // nom
        gc.setFont(FONT_NAME);
        gc.setFill(craftable ? Color.WHITE : Color.rgb(200, 200, 200));
        gc.fillText(r.getName(), iconX + ICON_BIG + 10, y + 22);

        // ingredients : pour chaque ressource il faut on affiche l'icone + "j'ai / il faut"
        double ingX = iconX + ICON_BIG + 10;
        double ingY = y + 30;
        gc.setFont(FONT_CNT);
        for (Map.Entry<Integer,Integer> cost : r.getCosts().entrySet()) {
            int id  = cost.getKey();
            int req = cost.getValue();
            int has = player.amountOf(id);

            ItemDefinition def = ItemRegistry.get(id);
            if (def != null && def.texture != null) {
                gc.drawImage(def.texture, ingX, ingY, ICON_SMALL, ICON_SMALL);
            }
            // vert si on a assez, rouge sinon
            gc.setFill(has >= req ? Color.rgb(170, 255, 170) : Color.rgb(255, 130, 130));
            gc.fillText(has + "/" + req, ingX + ICON_SMALL + 2, ingY + ICON_SMALL - 5);
            ingX += ICON_SMALL + 40;
        }
    }
}
