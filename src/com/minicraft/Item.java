package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Item {
	//position de l'item
    protected double x;
    protected double y;
    //id de l'item
    protected final ItemStack stack;
    protected final int itemId;
    private boolean removed = false;

    public Item(double x, double y, ItemStack stack) {
        this.x = x;
        this.y = y;
        this.stack = stack;
        this.itemId=stack.getItemId();
    }

    public abstract boolean isPlaceable();

    public void render(GraphicsContext gc) {
    	//effet flottant
        double floatingOffset = Math.sin(System.currentTimeMillis() * 0.005) * 4;
    
        ItemDefinition def = ItemRegistry.get(itemId);
        if (def != null && def.texture != null) {
            gc.drawImage(def.texture, x, y + floatingOffset, Config.itemSize, Config.itemSize);
        } else {
            // fallback si texture manquante
            gc.setFill(Color.PINK);
            gc.fillRect(x, y + floatingOffset, Config.itemSize, Config.itemSize);
        }
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
    public ItemStack getStack()  { return stack; }
    public double getX()         { return x; }
    public double getY()         { return y; }
    public double getCenterX()   { return x + Config.itemSize / 2; }
    public double getCenterY()   { return y + Config.itemSize / 2; }
    public boolean isRemoved()   { return removed; }
    public void remove()         { this.removed = true; }
}
