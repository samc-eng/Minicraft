package com.minicraft;

import java.util.Random;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SheepBot {
    private final Random random = new Random();
    private double x;
    private double y;
    private double directionX;
    private double directionY;
    private int decisionTicks;

    public SheepBot(double x, double y) {
        this.x = x;
        this.y = y;
        chooseNextMove();
    }

    public void tick(Level level) {
        if (decisionTicks <= 0) {
            chooseNextMove();
        }
        decisionTicks--;

        if (directionX == 0.0 && directionY == 0.0) {
            return;
        }

        double nextX = x + directionX * Config.SHEEP_SPEED;
        double nextY = y + directionY * Config.SHEEP_SPEED;
        boolean blocked = true;

        if (level.isEntityPositionValid(nextX, y)) {
            x = nextX;
            blocked = false;
        }

        if (level.isEntityPositionValid(x, nextY)) {
            y = nextY;
            blocked = false;
        }

        if (blocked) {
            chooseNextMove();
        }
    }

    private void chooseNextMove() {
        decisionTicks = Config.SHEEP_MIN_DIRECTION_TICKS
                + random.nextInt(Config.SHEEP_MAX_DIRECTION_TICKS - Config.SHEEP_MIN_DIRECTION_TICKS + 1);

        if (random.nextDouble() < Config.SHEEP_IDLE_CHANCE) {
            directionX = 0.0;
            directionY = 0.0;
            return;
        }

        double angle = random.nextDouble() * Math.PI * 2.0;
        directionX = Math.cos(angle);
        directionY = Math.sin(angle);
    }

    public void render(GraphicsContext gc) {
        double size = Config.blockSize;

        gc.setFill(Color.rgb(0, 0, 0, 0.20));
        gc.fillOval(x + 2, y + size - 4, size - 4, 4);

        gc.setFill(Color.rgb(65, 65, 65));
        gc.fillRect(x + 4, y + 11, 2, 4);
        gc.fillRect(x + 10, y + 11, 2, 4);

        gc.setFill(Color.rgb(238, 238, 228));
        gc.fillOval(x + 2, y + 4, 11, 8);
        gc.fillOval(x + 1, y + 3, 5, 5);
        gc.fillOval(x + 5, y + 2, 5, 5);
        gc.fillOval(x + 9, y + 3, 5, 5);

        gc.setFill(Color.rgb(150, 150, 145));
        gc.fillOval(x + 10, y + 5, 5, 5);

        gc.setFill(Color.BLACK);
        gc.fillOval(x + 13, y + 6, 1.5, 1.5);

        gc.setStroke(Color.rgb(210, 210, 200));
        gc.setLineWidth(1.0);
        gc.strokeOval(x + 2, y + 4, 11, 8);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getCenterX() {
        return x + Config.blockSize / 2.0;
    }

    public double getCenterY() {
        return y + Config.blockSize / 2.0;
    }
}
