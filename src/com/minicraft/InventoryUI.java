package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;

// Vue unifiée de l'inventaire :
//   - Ligne du haut (9 cases) = hotbar (bordure jaune)
//   - Lignes du dessous       = stockage (bordure grise)
// Cliquer sur une case de hotbar  -> envoie l'item au stockage
// Cliquer sur une case stockage   -> envoie l'item dans la 1re case vide de la hotbar
public class InventoryUI {
    private static final int COLS_HOTBAR  = 9;
    private static final int COLS_STORAGE = 9;
    private static final int ROWS_STORAGE = 2;
    private static final int SLOT_SIZE    = 64;
    private static final int SPACING      = 6;
    private static final int PADDING      = 28;
    private static final int TITLE_BAR    = 140; // titre + sous-titre + label HOTBAR
    private static final int LABEL_GAP    = 64;  // entre slots hotbar et slots stockage

    private static final Font FONT_TITLE = Font.font("Arial Rounded MT Bold", FontWeight.BOLD, 32);
    private static final Font FONT_HINT  = Font.font("Arial", 16);
    private static final Font FONT_LABEL = Font.font("Arial", FontWeight.BOLD, 22);
    private static final Font FONT_QTY   = Font.font("Arial", FontWeight.BOLD, 16);

    private double panelX, panelY, panelW, panelH;
    private double hotbarX, hotbarY;
    private double storageX, storageY;

    private void computeBounds(GraphicsContext gc) {
        double canvasW = gc.getCanvas().getWidth();
        double canvasH = gc.getCanvas().getHeight();

        double gridW = COLS_HOTBAR * SLOT_SIZE + (COLS_HOTBAR - 1) * SPACING;
        double hotbarRowH  = SLOT_SIZE;
        double storageRowsH = ROWS_STORAGE * SLOT_SIZE + (ROWS_STORAGE - 1) * SPACING;

        panelW = gridW + 2 * PADDING;
        panelH = TITLE_BAR + hotbarRowH + LABEL_GAP + storageRowsH + PADDING * 2;
        panelX = (canvasW - panelW) / 2.0;
        panelY = (canvasH - panelH) / 2.0;

        hotbarX  = panelX + PADDING;
        hotbarY  = panelY + TITLE_BAR;
        storageX = panelX + PADDING;
        storageY = hotbarY + hotbarRowH + LABEL_GAP;
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
        computeBounds(gc);

        // Voile assombrissant le jeu derrière
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        // Panneau
        gc.setFill(Color.rgb(40, 30, 20, 0.96));
        gc.fillRoundRect(panelX, panelY, panelW, panelH, 16, 16);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(3);
        gc.strokeRoundRect(panelX, panelY, panelW, panelH, 16, 16);

        // Bandeau de titre (séparé visuellement)
        gc.setFont(FONT_TITLE);
        gc.setFill(Color.GOLD);
        gc.fillText("INVENTAIRE", panelX + PADDING, panelY + 48);

        // Ligne séparatrice sous le titre
        gc.setStroke(Color.rgb(255, 215, 0, 0.4));
        gc.setLineWidth(1);
        gc.strokeLine(panelX + PADDING, panelY + 62,
                      panelX + panelW - PADDING, panelY + 62);

        // Sous-titre / indication
        gc.setFont(FONT_HINT);
        gc.setFill(Color.rgb(220, 220, 220));
        gc.fillText("Clic sur la HOTBAR = ranger    |    Clic sur le STOCKAGE = mettre en hotbar",
                    panelX + PADDING, panelY + 86);

        // Label HOTBAR (bien au-dessus des slots, pas collé au sous-titre)
        gc.setFont(FONT_LABEL);
        gc.setFill(Color.GOLD);
        gc.fillText("HOTBAR", hotbarX, hotbarY - 12);

        // Hotbar (ligne du haut, bordure jaune)
        ItemStack[] hotbar = player.getHotbar();
        for (int col = 0; col < COLS_HOTBAR; col++) {
            double sx = hotbarX + col * (SLOT_SIZE + SPACING);
            double sy = hotbarY;
            drawSlot(gc, sx, sy, hotbar[col], Color.GOLD);
        }

        // Label STOCKAGE
        gc.setFont(FONT_LABEL);
        gc.setFill(Color.WHITE);
        gc.fillText("STOCKAGE", storageX, storageY - 12);

        // Stockage (grille du bas, bordure grise)
        List<ItemStack> stacks = player.getInventory().getAll();
        for (int row = 0; row < ROWS_STORAGE; row++) {
            for (int col = 0; col < COLS_STORAGE; col++) {
                double sx = storageX + col * (SLOT_SIZE + SPACING);
                double sy = storageY + row * (SLOT_SIZE + SPACING);
                int idx = row * COLS_STORAGE + col;
                ItemStack s = (idx < stacks.size()) ? stacks.get(idx) : null;
                drawSlot(gc, sx, sy, s, Color.LIGHTGRAY);
            }
        }
    }

    private void drawSlot(GraphicsContext gc, double sx, double sy, ItemStack s, Color borderColor) {
        gc.setFill(Color.rgb(20, 20, 20, 0.85));
        gc.fillRoundRect(sx, sy, SLOT_SIZE, SLOT_SIZE, 6, 6);
        gc.setStroke(borderColor);
        gc.setLineWidth(2);
        gc.strokeRoundRect(sx, sy, SLOT_SIZE, SLOT_SIZE, 6, 6);
        if (s == null) return;

        ItemDefinition def = s.getDefinition();
        if (def != null && def.texture != null) {
            gc.drawImage(def.texture, sx + 8, sy + 8, SLOT_SIZE - 16, SLOT_SIZE - 16);
        }
        if (s.getAmount() > 1) {
            gc.setFont(FONT_QTY);
            gc.setFill(Color.WHITE);
            gc.fillText(String.valueOf(s.getAmount()), sx + SLOT_SIZE - 24, sy + SLOT_SIZE - 6);
        }
    }

    // Gère le clic à la position (mx, my). Renvoie true si une action a été faite.
    public boolean handleClick(Player player, double mx, double my) {
        // Test hotbar (ligne du haut)
        if (my >= hotbarY && my <= hotbarY + SLOT_SIZE) {
            int col = slotColumnAt(mx, hotbarX);
            if (col >= 0 && col < COLS_HOTBAR) {
                return clickHotbar(player, col);
            }
        }
        // Test stockage
        if (my >= storageY) {
            int rowSpan = SLOT_SIZE + SPACING;
            int row = (int) ((my - storageY) / rowSpan);
            double inRowY = (my - storageY) - row * rowSpan;
            if (row >= 0 && row < ROWS_STORAGE && inRowY <= SLOT_SIZE) {
                int col = slotColumnAt(mx, storageX);
                if (col >= 0 && col < COLS_STORAGE) {
                    return clickStorage(player, row * COLS_STORAGE + col);
                }
            }
        }
        return false;
    }

    private int slotColumnAt(double mx, double baseX) {
        if (mx < baseX) return -1;
        int colSpan = SLOT_SIZE + SPACING;
        int col = (int) ((mx - baseX) / colSpan);
        double inColX = (mx - baseX) - col * colSpan;
        if (inColX > SLOT_SIZE) return -1;
        return col;
    }

    // Hotbar -> stockage : retire de la hotbar, ajoute à l'inventaire.
    private boolean clickHotbar(Player player, int col) {
        ItemStack[] hotbar = player.getHotbar();
        ItemStack s = hotbar[col];
        if (s == null) return false;
        hotbar[col] = null;
        player.getInventory().add(s);
        return true;
    }

    // Stockage -> hotbar : retire de l'inventaire, met dans la 1re case vide de la hotbar.
    private boolean clickStorage(Player player, int idx) {
        List<ItemStack> stacks = player.getInventory().getAll();
        if (idx < 0 || idx >= stacks.size()) return false;

        ItemStack[] hotbar = player.getHotbar();
        for (int i = 0; i < hotbar.length; i++) {
            if (hotbar[i] == null) {
                hotbar[i] = stacks.get(idx);
                stacks.remove(idx);
                return true;
            }
        }
        return false; // hotbar pleine
    }
}
