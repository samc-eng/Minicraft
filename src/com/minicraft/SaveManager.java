package com.minicraft;

import java.io.*;
import java.util.Scanner;
import java.util.List;

public class SaveManager {
    private static final String SAVE_FILE = "save.txt";

    public static void saveGame(Player p, Main engine) {
        saveGameToFile(p, engine, SAVE_FILE);
    }

    // surcharge avec un nom de fichier custom (pratique pour les tests)
    public static void saveGameToFile(Player p, Main engine, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
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
            System.out.println("Sauvegarde OK -> " + filename);

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

    // reconstruit un niveau (surface ou grotte) depuis le scanner positionne au debut du bloc niveau
    public static Level readLevel(Scanner sc) {
        String[] dims = sc.nextLine().split(",");
        int w     = Integer.parseInt(dims[0]);
        int h     = Integer.parseInt(dims[1]);
        int depth = Integer.parseInt(dims[2]);
        long seed = Long.parseLong(dims[3]);

        String[] floorData = sc.nextLine().split(",");
        int[][] floor = new int[w][h];
        int idx = 0;
        for (int i = 0; i < w; i++) for (int j = 0; j < h; j++) floor[i][j] = Integer.parseInt(floorData[idx++]);

        String[] blocksData = sc.nextLine().split(",");
        int[][] blocks = new int[w][h];
        idx = 0;
        for (int i = 0; i < w; i++) for (int j = 0; j < h; j++) blocks[i][j] = Integer.parseInt(blocksData[idx++]);

        // on regenere le niveau avec le seed (ca remet les portails au bon endroit)
        // puis on remplace le sol et les blocs par ce qu'il y avait dans la save
        Level lvl = new Level(w, h, depth, seed);
        lvl.setFloorArray(floor);
        lvl.setBlocksArray(blocks);

        int nItems = Integer.parseInt(sc.nextLine().trim());
        for (int i = 0; i < nItems; i++) {
            String[] it = sc.nextLine().split(",");
            lvl.dropItem(Double.parseDouble(it[0]), Double.parseDouble(it[1]),
                         new ItemStack(Integer.parseInt(it[2]), 1));
        }

        int nBots = Integer.parseInt(sc.nextLine().trim());
        for (int i = 0; i < nBots; i++) {
            String[] bt = sc.nextLine().split(",");
            lvl.addBot(Double.parseDouble(bt[0]), Double.parseDouble(bt[1]));
        }

        return lvl;
    }

    public static Scanner getSaveScanner() {
        return getSaveScannerForFile(SAVE_FILE);
    }

    public static Scanner getSaveScannerForFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Pas de sauvegarde trouvee : " + filename);
            return null;
        }
        try {
            return new Scanner(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
