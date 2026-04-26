package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Item {
	//position de l'item
    protected double x;
    protected double y;
    //id de l'item
    protected final int itemId;
    private boolean removed = false;

    public Item(double x, double y, int itemId) {
        this.x = x;
        this.y = y;
        this.itemId = itemId;
    }

    public abstract boolean isPlaceable();

    public void render(GraphicsContext gc) {
    	//effet flottant
        double floatingOffset = Math.sin(System.currentTimeMillis() * 0.005) * 4;


        // Couleur selon l'item
        switch (itemId) {
            case 1  -> gc.setFill(Color.LIGHTGRAY);  // Pierre
            case 2  -> gc.setFill(Color.GRAY);        // Mur
            case 3  -> gc.setFill(Color.BROWN);       // Planche
            case 12 -> gc.setFill(Color.TAN);         // Pioche bois
            case 13 -> gc.setFill(Color.SILVER);      // Pioche fer
            default -> gc.setFill(Color.PINK);        // ID inconnu
        }

        gc.fillRect(x, y + floatingOffset, Config.itemSize, Config.itemSize);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.5);
        gc.strokeRect(x, y + floatingOffset, Config.itemSize, Config.itemSize);
    }

    public void tick(Player player) {
        double dx = player.getCenterX() - this.getCenterX();
        double dy = player.getCenterY() - this.getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 16) {
            double attractionSpeed = 0.01;
            this.x += dx * attractionSpeed;
            this.y += dy * attractionSpeed;
        }
    }

   
    public int getItemId()       { return itemId; }
    public double getX()         { return x; }
    public double getY()         { return y; }
    public double getCenterX()   { return x + Config.itemSize / 2; }
    public double getCenterY()   { return y + Config.itemSize / 2; }
    public boolean isRemoved()   { return removed; }
    public void remove()         { this.removed = true; }
}
