package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class MiniMap {

    private static final int SIZE   = 200;
    private static final int MARGIN = 10;
    private static final int BORDER = 2;

    // Couleurs ARGB par type de tuile
    private static final int COLOR_WATER  = argb( 40, 100, 200);
    private static final int COLOR_GRASS  = argb( 80, 160,  50);
    private static final int COLOR_SAND   = argb(220, 190, 100);
    private static final int COLOR_STONE  = argb(120, 120, 120);
    private static final int COLOR_TREE      = argb( 30,  90,  30);
    private static final int COLOR_ROCK      = argb( 85,  85,  85);
    private static final int COLOR_PORTAL    = argb(180,  60, 220); // violet : porte
    private static final int COLOR_OBSIDIAN  = argb( 60,  20,  80); // violet foncé : cadre

    private WritableImage image;
    private int mapWidth;
    private int mapHeight;
    private List<int[]> portals = new ArrayList<>(); // positions tuile [tx, ty]

    /**
     * Pré-génère l'image de la minimap. À appeler après chaque changement de niveau.
     */
    public void generate(int[][] floor, int[][] blocks, int mapWidth, int mapHeight,
                         List<int[]> portals) {
        this.mapWidth  = mapWidth;
        this.mapHeight = mapHeight;
        this.portals   = portals != null ? portals : new ArrayList<>();
        this.image     = new WritableImage(SIZE, SIZE);

        PixelWriter pw    = image.getPixelWriter();
        double      stepX = (double) mapWidth  / SIZE;
        double      stepY = (double) mapHeight / SIZE;

        for (int px = 0; px < SIZE; px++) {
            for (int py = 0; py < SIZE; py++) {
                int tx = (int)(px * stepX);
                int ty = (int)(py * stepY);
                pw.setArgb(px, py, tileToColor(floor[tx][ty], blocks[tx][ty]));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Rendu minimap (coin bas-droite)
    // -------------------------------------------------------------------------

    public void render(GraphicsContext gc,
                       double playerX, double playerY,
                       double camX,    double camY,
                       double viewW,   double viewH,
                       double screenW, double screenH) {

        if (image == null) return;

        double ox = screenW - SIZE - MARGIN;
        double oy = screenH - SIZE - MARGIN;

        gc.setFill(Color.color(0, 0, 0, 0.55));
        gc.fillRect(ox - BORDER, oy - BORDER, SIZE + BORDER * 2, SIZE + BORDER * 2);
        gc.drawImage(image, ox, oy, SIZE, SIZE);

        double totalW = mapWidth  * (double) Config.blockSize;
        double totalH = mapHeight * (double) Config.blockSize;

        // Rectangle de viewport
        double rectX = (camX / totalW) * SIZE + ox;
        double rectY = (camY / totalH) * SIZE + oy;
        gc.setStroke(Color.color(1, 1, 1, 0.8));
        gc.setLineWidth(1);
        gc.strokeRect(rectX, rectY,
                      Math.max(3, (viewW / totalW) * SIZE),
                      Math.max(3, (viewH / totalH) * SIZE));

        // Pings des portails
        drawPortalPings(gc, ox, oy, SIZE, totalW, totalH, 3.0, 6.0);

        // Point joueur
        double dotX = (playerX / totalW) * SIZE + ox;
        double dotY = (playerY / totalH) * SIZE + oy;
        gc.setFill(Color.RED);
        gc.fillOval(dotX - 2, dotY - 2, 4, 4);
    }

    // -------------------------------------------------------------------------
    // Rendu plein écran (TAB)
    // -------------------------------------------------------------------------

    public void renderFullscreen(GraphicsContext gc,
                                 double playerX, double playerY,
                                 double camX,    double camY,
                                 double viewW,   double viewH,
                                 double screenW, double screenH) {

        if (image == null) return;

        double mapSize = Math.min(screenW, screenH) * 0.82;
        double ox = (screenW - mapSize) / 2.0;
        double oy = (screenH - mapSize) / 2.0;

        gc.setFill(Color.color(0, 0, 0, 0.72));
        gc.fillRect(0, 0, screenW, screenH);

        gc.setStroke(Color.color(1, 1, 1, 0.9));
        gc.setLineWidth(3);
        gc.strokeRect(ox - 3, oy - 3, mapSize + 6, mapSize + 6);
        gc.drawImage(image, ox, oy, mapSize, mapSize);

        double totalW = mapWidth  * (double) Config.blockSize;
        double totalH = mapHeight * (double) Config.blockSize;

        // Rectangle de viewport
        double rectX = (camX / totalW) * mapSize + ox;
        double rectY = (camY / totalH) * mapSize + oy;
        gc.setStroke(Color.color(1, 1, 0, 0.9));
        gc.setLineWidth(2);
        gc.strokeRect(rectX, rectY,
                      Math.max(6, (viewW / totalW) * mapSize),
                      Math.max(6, (viewH / totalH) * mapSize));

        // Pings des portails (plus grands en plein écran)
        drawPortalPings(gc, ox, oy, mapSize, totalW, totalH, 6.0, 12.0);

        // Point joueur
        double dotX = (playerX / totalW) * mapSize + ox;
        double dotY = (playerY / totalH) * mapSize + oy;
        gc.setFill(Color.RED);
        gc.fillOval(dotX - 5, dotY - 5, 10, 10);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeOval(dotX - 5, dotY - 5, 10, 10);

        // Légende avec symbole portail
        gc.setFill(Color.color(1, 1, 1, 0.75));
        gc.setFont(javafx.scene.text.Font.font(13));
        gc.fillText("TAB — Fermer la carte    ⬤ Entrée de grotte", ox, oy + mapSize + 20);
    }

    // -------------------------------------------------------------------------
    // Pings animés pour les portails
    // -------------------------------------------------------------------------

    /**
     * Dessine un point violet fixe + un anneau pulsant pour chaque portail.
     * L'animation utilise System.currentTimeMillis() — aucun état nécessaire.
     */
    private void drawPortalPings(GraphicsContext gc,
                                 double ox, double oy, double displaySize,
                                 double totalW, double totalH,
                                 double dotRadius, double ringMaxRadius) {

        // Pulse : valeur entre 0.0 et 1.0, cycle de ~1.2 secondes
        double pulse = (Math.sin(System.currentTimeMillis() / 200.0) + 1.0) / 2.0;

        for (int[] portal : portals) {
            double px = (portal[0] * (double) Config.blockSize / totalW) * displaySize + ox;
            double py = (portal[1] * (double) Config.blockSize / totalH) * displaySize + oy;

            // Anneau pulsant (s'agrandit et s'estompe)
            double ringRadius = dotRadius + pulse * ringMaxRadius;
            gc.setStroke(Color.color(0.7, 0.2, 1.0, 1.0 - pulse * 0.8));
            gc.setLineWidth(1.5);
            gc.strokeOval(px - ringRadius, py - ringRadius, ringRadius * 2, ringRadius * 2);

            // Point central fixe
            gc.setFill(Color.color(0.75, 0.25, 1.0));
            gc.fillOval(px - dotRadius, py - dotRadius, dotRadius * 2, dotRadius * 2);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeOval(px - dotRadius, py - dotRadius, dotRadius * 2, dotRadius * 2);
        }
    }

    // -------------------------------------------------------------------------
    // Couleurs
    // -------------------------------------------------------------------------

    private static int tileToColor(int floorId, int blockId) {
        if (blockId != 0) {
            if (blockId == MapGenerator.BLOCK_TREE)          return COLOR_TREE;
            if (blockId == MapGenerator.BLOCK_CAVE_ENTRANCE) return COLOR_PORTAL;
            if (blockId == MapGenerator.BLOCK_OBSIDIAN)      return COLOR_OBSIDIAN;
            return COLOR_ROCK;
        }
        switch (floorId) {
            case MapGenerator.FLOOR_WATER: return COLOR_WATER;
            case MapGenerator.FLOOR_GRASS: return COLOR_GRASS;
            case MapGenerator.FLOOR_SAND:  return COLOR_SAND;
            case MapGenerator.FLOOR_STONE: return COLOR_STONE;
            default:                       return COLOR_STONE;
        }
    }

    private static int argb(int r, int g, int b) {
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }
}
