package com.minicraft;

import java.io.*;
import java.util.Scanner;

public class SaveManager {
    private static final String SAVE_FILE = "save.txt";

    /**
     * Sauvegarde complète : Joueur + Inventaire + Map
     */
    public static void saveGame(Player p, Level lvl) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_FILE))) {
            // 1. Sauvegarde du Joueur : X, Y, Roche, Bois
            writer.println(p.getX() + "," + p.getY() + "," +
                    p.getInventory().getAmount(1) + "," +
                    p.getInventory().getAmount(2));

            // 2. Sauvegarde des dimensions du monde
            int w = lvl.getWidth();
            int h = lvl.getHeight();
            writer.println(w + "," + h);

            // 3. Sauvegarde du sol (floor)
            int[][] floor = lvl.getFloorArray();
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    writer.print(floor[i][j] + ",");
                }
            }
            writer.println(); // Saut de ligne après le sol

            // 4. Sauvegarde des obstacles (blocks : arbres, roches)
            int[][] blocks = lvl.getBlocksArray();
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    writer.print(blocks[i][j] + ",");
                }
            }
            writer.println();

            System.out.println("✅ Monde et Joueur sauvegardés !");
        } catch (IOException e) {
            System.err.println("❌ Erreur de sauvegarde : " + e.getMessage());
        }
    }

    public static Scanner getSaveScanner() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return null;
        try {
            return new Scanner(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}