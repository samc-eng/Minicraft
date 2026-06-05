package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Bot extends EnemyBot {
    
    private double vitesse = Config.ENEMY_BOT_SPEED;
    private int attackCooldown = 0;
    private static final Image BOT_SPRITE = new Image(
        new java.io.File("resources/bot.png").toURI().toString()
    );

    public Bot(double x, double y) {
        super(x, y);
    }

    public void tick(Level level, Player player) {
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        moveTowardPlayer(level, player, vitesse, 0);

        if (isTouchingPlayer(player) && attackCooldown == 0) {
            boolean aPrisDesDegats = player.takeDamage(1);
            if (aPrisDesDegats) {
                attackCooldown = 60; 
            } else {
                attackCooldown = 10;
            }
        }
    }

    public void render(GraphicsContext gc) {
        gc.drawImage(BOT_SPRITE, x, y, Config.blockSize, Config.blockSize);
    }
}
