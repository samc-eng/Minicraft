package com.minicraft;

import java.io.*;
import java.util.Scanner;
import java.util.List;

public class SaveManager {
    private static final String SAVE_FILE = "save.txt";

    public static void saveGame(Player p, Level lvl) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_FILE))) {
            // 1. Joueur
            writer.println(p.getX() + "," + p.getY() + "," +
                    p.getInventory().getAmount(1) + "," +
                    p.getInventory().getAmount(2));

            // 2. Dimensions
            int w = lvl.getWidth();
            int h = lvl.getHeight();
            writer.println(w + "," + h);

            // 3. Sol
            int[][] floor = lvl.getFloorArray();
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    writer.print(floor[i][j] + (i == w - 1 && j == h - 1 ? "" : ","));
                }
            }
            writer.println();

            // 4. Blocs
            int[][] blocks = lvl.getBlocksArray();
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    writer.print(blocks[i][j] + (i == w - 1 && j == h - 1 ? "" : ","));
                }
            }
            writer.println();

            // 5. Items au sol
            List<Item> itemsAuSol = lvl.getItems();
            writer.println(itemsAuSol.size());
            for (Item it : itemsAuSol) {
                writer.println(it.getX() + "," + it.getY() + "," + it.getItemId());
            }

            // --- 6. Sauvegarde des Bots (Vérifiée) ---
            List<Bot> bots = lvl.getBots();
            System.out.println("DEBUG SAVE: Tentative de sauvegarde de " + bots.size() + " bots.");

            writer.println(bots.size());
            for (Bot bot : bots) {
                // On s'assure d'écrire des coordonnées valides
                writer.println(bot.getX() + "," + bot.getY());
            }

            // Forcer l'écriture sur le disque
            writer.flush();
            System.out.println("✅ Sauvegarde réussie dans " + SAVE_FILE);

        } catch (IOException e) {
            System.err.println("❌ Erreur critique de sauvegarde : " + e.getMessage());
        }
    }

    public static Scanner getSaveScanner() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            System.out.println("⚠️ Aucun fichier de sauvegarde trouvé.");
            return null;
        }
        try {
            return new Scanner(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}