package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;

// fenetre d'inventaire (touche E)
// ligne du haut = hotbar, en dessous = stockage
// on clique sur une case pour faire passer l'item de l'un a l'autre
public class InventoryUI {
    // tailles de la grille
    private static final int COLS_HOTBAR  = 9;
    private static final int COLS_STORAGE = 9;
    private static final int ROWS_STORAGE = 2;
    private static final int SLOT_SIZE    = 64;
    private static final int SPACING      = 6;
    private static final int PADDING      = 28;
    private static final int TITLE_BAR    = 140;
    private static final int LABEL_GAP    = 64;

    private static final Font FONT_TITLE = Font.font("Arial Rounded MT Bold", FontWeight.BOLD, 32);
    private static final Font FONT_HINT  = Font.font("Arial", 16);
    private static final Font FONT_LABEL = Font.font("Arial", FontWeight.BOLD, 22);
    private static final Font FONT_QTY   = Font.font("Arial", FontWeight.BOLD, 16);

    private double panelX, panelY, panelW, panelH;
    private double hotbarX, hotbarY;
    private double storageX, storageY;
    private double armorX, armorY;   // emplacement d'armure (en haut à droite du panneau)

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

        // L'emplacement d'armure se place dans la barre de titre, à droite.
        armorX = panelX + panelW - PADDING - SLOT_SIZE;
        armorY = panelY + 24;
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

        // on noircit le fond derriere
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        // la boite
        gc.setFill(Color.rgb(40, 30, 20, 0.96));
        gc.fillRoundRect(panelX, panelY, panelW, panelH, 16, 16);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(3);
        gc.strokeRoundRect(panelX, panelY, panelW, panelH, 16, 16);

        // titre INVENTAIRE
        gc.setFont(FONT_TITLE);
        gc.setFill(Color.GOLD);
        gc.fillText("INVENTAIRE", panelX + PADDING, panelY + 48);

        // petite ligne dorée sous le titre
        gc.setStroke(Color.rgb(255, 215, 0, 0.4));
        gc.setLineWidth(1);
        gc.strokeLine(panelX + PADDING, panelY + 62,
                      panelX + panelW - PADDING, panelY + 62);

        // texte d'aide
        gc.setFont(FONT_HINT);
        gc.setFill(Color.rgb(220, 220, 220));
        gc.fillText("Clic sur la HOTBAR = ranger    |    Clic sur le STOCKAGE = mettre en hotbar",
                    panelX + PADDING, panelY + 86);

        // label de la hotbar
        gc.setFont(FONT_LABEL);
        gc.setFill(Color.GOLD);
        gc.fillText("HOTBAR", hotbarX, hotbarY - 12);

        // les 9 cases de la hotbar avec bordure jaune
        ItemStack[] hotbar = player.getHotbar();
        for (int col = 0; col < COLS_HOTBAR; col++) {
            double sx = hotbarX + col * (SLOT_SIZE + SPACING);
            double sy = hotbarY;
            drawSlot(gc, sx, sy, hotbar[col], Color.GOLD);
        }

        // label du stockage
        gc.setFont(FONT_LABEL);
        gc.setFill(Color.WHITE);
        gc.fillText("STOCKAGE", storageX, storageY - 12);

        // les cases du stockage avec bordure grise
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

        // --- EMPLACEMENT D'ARMURE (bordure cyan) ---
        ArmorSlots armor = player.getArmorSlots();

        gc.setFont(FONT_LABEL);
        gc.setFill(Color.CYAN);
        gc.fillText("ARMURE", armorX - 14, armorY - 8);

        gc.setFill(Color.rgb(20, 20, 20, 0.85));
        gc.fillRoundRect(armorX, armorY, SLOT_SIZE, SLOT_SIZE, 6, 6);
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(2);
        gc.strokeRoundRect(armorX, armorY, SLOT_SIZE, SLOT_SIZE, 6, 6);

        if (armor.hasArmor()) {
            ItemDefinition def = ItemRegistry.get(armor.getEquipped());
            if (def != null && def.texture != null) {
                gc.drawImage(def.texture, armorX + 8, armorY + 8, SLOT_SIZE - 16, SLOT_SIZE - 16);
            }
            // pourcentage de tank affiché sous le slot
            gc.setFont(FONT_QTY);
            gc.setFill(Color.CYAN);
            gc.fillText((int)(armor.getTankChance() * 100) + "%", armorX + 6, armorY + SLOT_SIZE + 18);
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

    // appelle quand on clique pour savoir quelle case a ete touchee
    public boolean handleClick(Player player, double mx, double my) {
        // clic sur l'emplacement d'armure ? -> on déséquipe vers le stockage
        if (mx >= armorX && mx <= armorX + SLOT_SIZE && my >= armorY && my <= armorY + SLOT_SIZE) {
            return unequipArmor(player);
        }
        // clic dans la hotbar ?
        if (my >= hotbarY && my <= hotbarY + SLOT_SIZE) {
            int col = slotColumnAt(mx, hotbarX);
            if (col >= 0 && col < COLS_HOTBAR) {
                return clickHotbar(player, col);
            }
        }
        // clic dans le stockage ?
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

    // on prend l'item de la hotbar et on le met dans le stockage
    // (cas special : si c'est une armure, on l'equipe directement)
    private boolean clickHotbar(Player player, int col) {
        ItemStack[] hotbar = player.getHotbar();
        ItemStack s = hotbar[col];
        if (s == null) return false;

        if (ArmorSlots.isArmor(s.getItemId())) {
            equipFrom(player, s);   // equipe et gere l'echange avec l'ancienne armure
            hotbar[col] = null;
            return true;
        }

        hotbar[col] = null;
        player.getInventory().add(s);
        return true;
    }

    // on prend un item du stockage et on le met dans la premiere case vide de la hotbar
    // (cas special : si c'est une armure, on l'equipe directement)
    private boolean clickStorage(Player player, int idx) {
        List<ItemStack> stacks = player.getInventory().getAll();
        if (idx < 0 || idx >= stacks.size()) return false;

        ItemStack s = stacks.get(idx);

        if (ArmorSlots.isArmor(s.getItemId())) {
            stacks.remove(idx);
            equipFrom(player, s);
            return true;
        }

        ItemStack[] hotbar = player.getHotbar();
        for (int i = 0; i < hotbar.length; i++) {
            if (hotbar[i] == null) {
                hotbar[i] = stacks.get(idx);
                stacks.remove(idx);
                return true;
            }
        }
        return false; // pas de place
    }

    /**
     * Équipe l'armure `s` ; si une armure était déjà portée, elle repart dans le stockage.
     */
    private void equipFrom(Player player, ItemStack s) {
        int previous = player.getArmorSlots().equip(s.getItemId());
        if (previous != ArmorSlots.EMPTY) {
            player.getInventory().add(new ItemStack(previous, 1));
        }
    }

    /** Retire l'armure équipée et la renvoie dans le stockage. */
    private boolean unequipArmor(Player player) {
        int id = player.getArmorSlots().unequip();
        if (id == ArmorSlots.EMPTY) return false;
        player.getInventory().add(new ItemStack(id, 1));
        return true;
    }
}
