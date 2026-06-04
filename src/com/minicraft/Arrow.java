package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Arrow {
    private double x;
    private double y;
    private final double vx;
    private final double vy;
    private double distanceTravelled = 0.0;
    private boolean removed = false;

    public Arrow(double startX, double startY, double targetX, double targetY) {
        this.x = startX;
        this.y = startY;

        double dx = targetX - startX;
        double dy = targetY - startY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < 0.001) {
            distance = 1.0;
        }

        this.vx = dx / distance * Config.ARROW_SPEED;
        this.vy = dy / distance * Config.ARROW_SPEED;
    }

    public void tick(Level level, Player player) {
        if (removed) return;

        int steps = Math.max(1, (int) Math.ceil(Config.ARROW_SPEED / Math.max(1.0, Config.ARROW_RADIUS)));
        double stepX = vx / steps;
        double stepY = vy / steps;
        double stepDistance = Math.sqrt(stepX * stepX + stepY * stepY);

        for (int i = 0; i < steps; i++) {
            x += stepX;
            y += stepY;
            distanceTravelled += stepDistance;

            if (hasExpired(level)) {
                removed = true;
                return;
            }

            if (touchesPlayer(player)) {
                player.takeDamage(Config.ARROW_DAMAGE);
                removed = true;
                return;
            }
        }
    }

    private boolean hasExpired(Level level) {
        double radius = Config.ARROW_RADIUS;
        return distanceTravelled >= Config.ARROW_MAX_DISTANCE
                || !level.isInsideMap(x, y)
                || level.isAreaBlocked(x - radius, y - radius, radius * 2, radius * 2);
    }

    private boolean touchesPlayer(Player player) {
        double radius = Config.ARROW_RADIUS;

        double leftA = x - radius;
        double rightA = x + radius;
        double topA = y - radius;
        double bottomA = y + radius;

        double leftB = player.getX();
        double rightB = player.getX() + Config.blockSize;
        double topB = player.getY();
        double bottomB = player.getY() + Config.blockSize;

        return !(leftA >= rightB || rightA <= leftB || topA >= bottomB || bottomA <= topB);
    }

    public void render(GraphicsContext gc) {
        double tailX = x - vx * 1.8;
        double tailY = y - vy * 1.8;
        double tipX = x + vx * 1.2;
        double tipY = y + vy * 1.2;

        gc.setStroke(Color.rgb(91, 58, 31));
        gc.setLineWidth(2);
        gc.strokeLine(tailX, tailY, tipX, tipY);

        gc.setFill(Color.LIGHTGRAY);
        gc.fillOval(tipX - 1.5, tipY - 1.5, 3, 3);
    }

    public boolean isRemoved() {
        return removed;
    }
}
