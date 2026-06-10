package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class HUD {
    private static final Image hudSheet = new Image(new java.io.File("resources/hud.png").toURI().toString());
    private static final int SPRITE_SIZE = 8;
    private static final int DISPLAY_SIZE = 16;
    private static final int MARGIN = 4;

    public void render(GraphicsContext gc, Player player, double screenW, double screenH) {
        int maxHealth = player.getMaxHealth();
        int health = Math.max(0, Math.min(player.getHealth(), maxHealth));
        int energy = Math.max(0, Math.min(player.getEnergy(), 10));

        for (int i = 0; i < maxHealth; i++) {
            int srcY = (i < health) ? 0 : 8;
            gc.drawImage(hudSheet,
                0, srcY, SPRITE_SIZE, SPRITE_SIZE,
                10 + i * (DISPLAY_SIZE + MARGIN), 10, DISPLAY_SIZE, DISPLAY_SIZE
            );
        }

        for (int i = 0; i < 10; i++) {
            int srcYenergy = (i < energy) ? 0 : 8;
            gc.drawImage(hudSheet,
                8, srcYenergy, SPRITE_SIZE, SPRITE_SIZE,
                10 + i * (DISPLAY_SIZE + MARGIN), 30, DISPLAY_SIZE, DISPLAY_SIZE
            );
        }

        int slotSize = 40;
        int spacing = 4;
        int totalWidth = 9 * slotSize + 8 * spacing;

        // On centre la hotbar par rapport à la taille RÉELLE du canvas (et non
        // une constante fixe), pour qu'elle reste centrée même après redimensionnement.
        double startX = (screenW - totalWidth) / 2.0;
        double startY = screenH - slotSize - 10;

        ItemStack[] hotbar = player.getHotbar();
        int activeSlot = player.getSelectedSlot();

        for (int i = 0; i < 9; i++) {
            double x = startX + i * (slotSize + spacing);
            double y = startY;

            gc.setFill(Color.rgb(40, 40, 40, 0.7));
            gc.fillRect(x, y, slotSize, slotSize);

            if (i == activeSlot) {
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(3);
            } else {
                gc.setStroke(Color.GRAY);
                gc.setLineWidth(1);
            }
            gc.strokeRect(x, y, slotSize, slotSize);

            if (hotbar[i] != null) {
                ItemDefinition def = hotbar[i].getDefinition();

                if (def != null && def.texture != null) {
                    gc.drawImage(def.texture, x + 4, y + 4, slotSize - 8, slotSize - 8);
                }

                if (hotbar[i].getAmount() > 1) {
                    gc.setFill(Color.WHITE);
                    gc.fillText(String.valueOf(hotbar[i].getAmount()), x + slotSize - 15, y + slotSize - 5);
                }
            }
        }
    }
}
