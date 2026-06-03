package com.minicraft;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

public class SaveManager {
    private static final String SAVE_FILE = "save.txt";

    public static void saveGame(Player p, Level lvl) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_FILE))) {
            writer.println(p.getX() + "," + p.getY() + ","
                    + p.getInventory().getAmount(1) + ","
                    + p.getInventory().getAmount(2) + ","
                    + p.getHealth());

            int w = lvl.getWidth();
            int h = lvl.getHeight();
            writer.println(w + "," + h);

            int[][] floor = lvl.getFloorArray();
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    writer.print(floor[i][j] + (i == w - 1 && j == h - 1 ? "" : ","));
                }
            }
            writer.println();

            int[][] blocks = lvl.getBlocksArray();
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    writer.print(blocks[i][j] + (i == w - 1 && j == h - 1 ? "" : ","));
                }
            }
            writer.println();

            List<Item> itemsAuSol = lvl.getItems();
            writer.println(itemsAuSol.size());
            for (Item it : itemsAuSol) {
                writer.println(it.getX() + "," + it.getY() + "," + it.getItemId());
            }

            List<Bot> bots = lvl.getBots();
            writer.println(bots.size());
            for (Bot bot : bots) {
                writer.println(bot.getX() + "," + bot.getY());
            }

            List<ArcherBot> archers = lvl.getArcherBots();
            writer.println(archers.size());
            for (ArcherBot archer : archers) {
                writer.println(archer.getX() + "," + archer.getY());
            }

            writer.flush();
            System.out.println("Sauvegarde reussie dans " + SAVE_FILE);
        } catch (IOException e) {
            System.err.println("Erreur critique de sauvegarde : " + e.getMessage());
        }
    }

    public static Scanner getSaveScanner() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            System.out.println("Aucun fichier de sauvegarde trouve.");
            return null;
        }
        try {
            return new Scanner(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
