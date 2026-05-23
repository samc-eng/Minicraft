package com.minicraft;

import java.io.*;
import java.util.Scanner;
import java.util.List;

public class SaveManager {
    private static final String SAVE_FILE = "save.txt";

    public static void saveGame(Player p, Main engine) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_FILE))) {
            // position du joueur
            writer.println(p.getX() + "," + p.getY());

            // a quelle profondeur on est et par quel portail on est descendu
            int[] lp = engine.getLastSurfacePortal();
            int lpx = (lp != null) ? lp[0] : -1;
            int lpy = (lp != null) ? lp[1] : -1;
            writer.println(engine.getCurrentDepth() + "," + lpx + "," + lpy);

            // la surface : on la sauve tout le temps meme si on est dans une grotte
            writeLevel(writer, engine.getSurfaceLevel());

            // la grotte si elle a deja ete visitee (sinon on met 0)
            Level cave = engine.getUndergroundLevels()[0];
            if (cave == null) {
                writer.println(0);
            } else {
                writer.println(1);
                writeLevel(writer, cave);
            }

            // toutes les piles dans l'inventaire (id, quantite, durabilite)
            List<ItemStack> invStacks = p.getInventory().getAll();
            writer.println(invStacks.size());
            for (ItemStack s : invStacks) {
                writer.println(s.getItemId() + "," + s.getAmount() + "," + s.getCurrentDurability());
            }

            // les 9 cases de la hotbar (un "-" si la case est vide)
            ItemStack[] hotbar = p.getHotbar();
            StringBuilder hb = new StringBuilder();
            for (int i = 0; i < hotbar.length; i++) {
                if (i > 0) hb.append(";");
                if (hotbar[i] == null) {
                    hb.append("-");
                } else {
                    hb.append(hotbar[i].getItemId())
                      .append(",").append(hotbar[i].getAmount())
                      .append(",").append(hotbar[i].getCurrentDurability());
                }
            }
            writer.println(hb);

            writer.flush();
            System.out.println("Sauvegarde OK -> " + SAVE_FILE);

        } catch (IOException e) {
            System.err.println("Erreur de sauvegarde : " + e.getMessage());
        }
    }

    // ecrit un niveau dans le fichier : taille + seed, sol, blocs, items, bots
    private static void writeLevel(PrintWriter writer, Level lvl) {
        int w = lvl.getWidth();
        int h = lvl.getHeight();
        writer.println(w + "," + h + "," + lvl.getDepth() + "," + lvl.getSeed());

        int[][] floor = lvl.getFloorArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (sb.length() > 0) sb.append(",");
                sb.append(floor[i][j]);
            }
        }
        writer.println(sb);

        int[][] blocks = lvl.getBlocksArray();
        sb = new StringBuilder();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (sb.length() > 0) sb.append(",");
                sb.append(blocks[i][j]);
            }
        }
        writer.println(sb);

        List<Item> items = lvl.getItems();
        writer.println(items.size());
        for (Item it : items) {
            writer.println(it.getX() + "," + it.getY() + "," + it.getItemId());
        }

        List<Bot> bots = lvl.getBots();
        writer.println(bots.size());
        for (Bot bot : bots) {
            writer.println(bot.getX() + "," + bot.getY());
        }
    }

    public static Scanner getSaveScanner() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            System.out.println("Pas de sauvegarde trouvee.");
            return null;
        }
        try {
            return new Scanner(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
