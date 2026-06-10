package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class ArcherBot extends EnemyBot {
    private static final Image SPRITE = Resources.image("bot.png");

    private int shootCooldown;

    public ArcherBot(double x, double y) {
        super(x, y);
        this.shootCooldown = (int) (Math.random() * Config.ARCHER_SHOOT_COOLDOWN_TICKS);
    }

    public Arrow tick(Level level, Player player) {
        if (shootCooldown > 0) {
            shootCooldown--;
        }

        moveTowardPlayer(level, player, Config.ENEMY_BOT_SPEED, Config.ARCHER_KEEP_DISTANCE);

        if (canShoot(level, player)) {
            shootCooldown = Config.ARCHER_SHOOT_COOLDOWN_TICKS;
            return new Arrow(getCenterX(), getCenterY(), player.getCenterX(), player.getCenterY());
        }

        return null;
    }

    private boolean canShoot(Level level, Player player) {
        if (shootCooldown > 0) return false;

        double dx = player.getCenterX() - getCenterX();
        double dy = player.getCenterY() - getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= Config.ARCHER_SHOOT_RANGE && hasLineOfSight(level, player);
    }

    private boolean hasLineOfSight(Level level, Player player) {
        double startX = getCenterX();
        double startY = getCenterY();
        double dx = player.getCenterX() - startX;
        double dy = player.getCenterY() - startY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        int steps = Math.max(1, (int) (distance / 4.0));

        for (int i = 1; i < steps; i++) {
            double t = i / (double) steps;
            double px = startX + dx * t;
            double py = startY + dy * t;
            if (level.isSolid(px, py)) {
                return false;
            }
        }

        return true;
    }

    public void render(GraphicsContext gc) {
        gc.drawImage(SPRITE, x, y, Config.blockSize, Config.blockSize);

        gc.setFill(Color.rgb(45, 95, 56, 0.65));
        gc.fillRect(x + 2, y + 2, Config.blockSize - 4, Config.blockSize - 4);

        gc.setStroke(Color.rgb(95, 55, 25));
        gc.setLineWidth(1.5);
        gc.strokeLine(x + 12, y + 3, x + 12, y + 13);
        gc.strokeLine(x + 9, y + 8, x + 14, y + 8);
    }
}
