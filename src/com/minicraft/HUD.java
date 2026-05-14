package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

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

    }
}
