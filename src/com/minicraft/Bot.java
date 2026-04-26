package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;


public class Bot {
    private double x;
    private double y;
    private double vitesse = 0.18;
    private int attackCooldown = 0;
    private static final Image BOT_SPRITE = new Image(
        new java.io.File("resources/bot.png").toURI().toString()
    );

    public Bot(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void tick(Level level, Player player) {
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        double dx = player.getCenterX() - getCenterX();
        double dy = player.getCenterY() - getCenterY();

        double futurX = x;
        double futurY = y;

        // poursuite simple : on suit l'axe le plus important
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 1) futurX += vitesse;
            if (dx < -1) futurX -= vitesse;
        } else {
            if (dy > 1) futurY += vitesse;
            if (dy < -1) futurY -= vitesse;
        }

        // tentative X seule
        if (!isBlocked(level, futurX, y)) {
            x = futurX;
        }

        // tentative Y seule
        if (!isBlocked(level, x, futurY)) {
            y = futurY;
        }

        // attaque au contact
        if (isTouching(player) && attackCooldown == 0) {
            player.takeDamage(1);
            attackCooldown = 45;
        }
    }

    private boolean isBlocked(Level level, double futurX, double futurY) {
        return level.getBlocks(futurX, futurY) >= 1
                || level.getBlocks(futurX + Config.blockSize, futurY) >= 1
                || level.getBlocks(futurX, futurY + Config.blockSize) >= 1
                || level.getBlocks(futurX + Config.blockSize, futurY + Config.blockSize) >= 1;
    }

    private boolean isTouching(Player player) {
        double leftA = x;
        double rightA = x + Config.blockSize;
        double topA = y;
        double bottomA = y + Config.blockSize;

        double leftB = player.getX();
        double rightB = player.getX() + Config.blockSize;
        double topB = player.getY();
        double bottomB = player.getY() + Config.blockSize;

        return !(leftA >= rightB || rightA <= leftB || topA >= bottomB || bottomA <= topB);
    }

    public void render(GraphicsContext gc) {
        gc.drawImage(BOT_SPRITE, x, y, Config.blockSize, Config.blockSize);
    }

    public double getCenterX() {
        return x + Config.blockSize / 2.0;
    }

    public double getCenterY() {
        return y + Config.blockSize / 2.0;
    }
}