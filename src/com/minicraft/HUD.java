package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class HUD {
    private static final Image hudSheet = new Image(new java.io.File("resources/hud.png").toURI().toString());
    private static final int SPRITE_SIZE = 8;
    private static final int DISPLAY_SIZE = 16; // taille affichée à l'écran
    private static final int MARGIN = 4;        // espace entre chaque icône

    public void render(GraphicsContext gc, Player player) {
        int health = player.getHealth();
        int energy = player.getEnergy();

        for (int i = 0; i < 10; i++) {
            int srcY = (i < health) ? 0 : 8; // plein ou vide
            int srcYenergy = (i < energy) ? 0 : 8; // plein ou vide
            //coeurs
            gc.drawImage(hudSheet,
                0, srcY, SPRITE_SIZE, SPRITE_SIZE, // source dans la spritesheet
                10 + i * (DISPLAY_SIZE + MARGIN), 10, DISPLAY_SIZE, DISPLAY_SIZE // position écran
            );

            //energie
            gc.drawImage(hudSheet,
                8, srcYenergy, SPRITE_SIZE, SPRITE_SIZE, // source dans la spritesheet
                10 + i * (DISPLAY_SIZE + MARGIN), 30, DISPLAY_SIZE, DISPLAY_SIZE // position écran
            );
        }

        int slotSize = 40; 
        int spacing = 4;   
        int totalWidth = 9 * slotSize + 8 * spacing;
        
        double startX = (Config.SCREEN_WIDTH - totalWidth) / 2.0;
        double startY = Config.SCREEN_HEIGHT - slotSize - 10; 

        ItemStack[] hotbar = player.getHotbar();
        int activeSlot = player.getSelectedSlot();

        for (int i = 0; i < 9; i++) {
            double x = startX + i * (slotSize + spacing);
            double y = startY;

            // 1. Fond
            gc.setFill(Color.rgb(40, 40, 40, 0.7)); 
            gc.fillRect(x, y, slotSize, slotSize);

            // 2. Bordure (Jaune si sélectionnée)
            if (i == activeSlot) {
                gc.setStroke(Color.YELLOW); 
                gc.setLineWidth(3);
            } else {
                gc.setStroke(Color.GRAY);   
                gc.setLineWidth(1);
            }
            gc.strokeRect(x, y, slotSize, slotSize);

            // 3. Objet et Quantité
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
