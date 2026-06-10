package com.minicraft;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class EnemyBot {
    private static final int[][] DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    protected double x;
    protected double y;
    protected int maxHealth = 10;
    protected int health = maxHealth;

    private int pathCooldown = 0;
    private List<int[]> path = new ArrayList<>();

    protected EnemyBot(double x, double y) {
        this.x = x;
        this.y = y;
    }

    protected void moveTowardPlayer(Level level, Player player, double speed, double stopDistance) {
        double dx = player.getCenterX() - getCenterX();
        double dy = player.getCenterY() - getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance <= stopDistance) {
            return;
        }

        if (pathCooldown <= 0 || path.isEmpty()) {
            path = findPath(level, getTileX(), getTileY(), player.getTileX(), player.getTileY());
            pathCooldown = Config.ENEMY_PATH_RECALC_TICKS;
        } else {
            pathCooldown--;
        }

        followPath(level, player, speed);
    }

    private void followPath(Level level, Player player, double speed) {
        while (!path.isEmpty() && isNearTile(path.get(0)[0], path.get(0)[1])) {
            path.remove(0);
        }

        if (path.isEmpty()) {
            moveDirectly(level, player.getCenterX(), player.getCenterY(), speed);
            return;
        }

        int[] nextTile = path.get(0);
        double targetX = nextTile[0] * Config.blockSize + Config.blockSize / 2.0;
        double targetY = nextTile[1] * Config.blockSize + Config.blockSize / 2.0;
        moveDirectly(level, targetX, targetY, speed);
    }

    private void moveDirectly(Level level, double targetX, double targetY, double speed) {
        double dx = targetX - getCenterX();
        double dy = targetY - getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < 0.001) {
            return;
        }

        double stepX = dx / distance * speed;
        double stepY = dy / distance * speed;
        moveBy(level, stepX, stepY);
    }

    private void moveBy(Level level, double stepX, double stepY) {
        boolean moved = false;
        double nextX = x + stepX;
        double nextY = y + stepY;

        // On vérifie les blocs solides ET l'eau sur l'axe X
        if (!level.isAreaBlocked(nextX, y, Config.blockSize, Config.blockSize) && !isTargetingWater(level, nextX, y)) {
            x = nextX;
            moved = true;
        }

        // On vérifie les blocs solides ET l'eau sur l'axe Y
        if (!level.isAreaBlocked(x, nextY, Config.blockSize, Config.blockSize) && !isTargetingWater(level, x, nextY)) {
            y = nextY;
            moved = true;
        }

        if (!moved) {
            path.clear();
            pathCooldown = 0;
        }
    }

    private boolean isTargetingWater(Level level, double futurX, double futurY) {
        int idEau = 29;
        
        // On calcule la case de la grille où le monstre s'apprête à poser le pied (en regardant son centre)
        int gridX = (int) ((futurX + Config.blockSize / 2) / Config.blockSize);
        int gridY = (int) ((futurY + Config.blockSize / 2) / Config.blockSize);

        // On vérifie que c'est bien dans les limites de la carte
        if (gridX >= 0 && gridX < level.getWidth() && gridY >= 0 && gridY < level.getHeight()) {
            int[][] floor = level.getFloorArray();
            return floor[gridX][gridY] == idEau;
        }
        return false;
    }

    private List<int[]> findPath(Level level, int startTx, int startTy, int targetTx, int targetTy) {
        if (!level.isWalkableTile(startTx, startTy)) {
            return new ArrayList<>();
        }

        int radius = Config.ENEMY_PATH_RADIUS;
        int minX = Math.max(0, startTx - radius);
        int minY = Math.max(0, startTy - radius);
        int maxX = Math.min(level.getWidth() - 1, startTx + radius);
        int maxY = Math.min(level.getHeight() - 1, startTy + radius);
        int localWidth = maxX - minX + 1;
        int localHeight = maxY - minY + 1;

        boolean[][] visited = new boolean[localWidth][localHeight];
        int[][] parentX = new int[localWidth][localHeight];
        int[][] parentY = new int[localWidth][localHeight];
        for (int i = 0; i < localWidth; i++) {
            Arrays.fill(parentX[i], -1);
            Arrays.fill(parentY[i], -1);
        }

        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{startTx, startTy});
        visited[startTx - minX][startTy - minY] = true;

        int bestTx = startTx;
        int bestTy = startTy;
        int bestScore = distanceScore(startTx, startTy, targetTx, targetTy);
        boolean reachedTarget = false;

        while (!queue.isEmpty()) {
            int[] current = queue.removeFirst();
            int currentScore = distanceScore(current[0], current[1], targetTx, targetTy);
            if (currentScore < bestScore) {
                bestScore = currentScore;
                bestTx = current[0];
                bestTy = current[1];
            }

            if (current[0] == targetTx && current[1] == targetTy) {
                bestTx = current[0];
                bestTy = current[1];
                reachedTarget = true;
                break;
            }

            for (int[] direction : DIRECTIONS) {
                int nx = current[0] + direction[0];
                int ny = current[1] + direction[1];
                if (nx < minX || ny < minY || nx > maxX || ny > maxY) continue;
                int lx = nx - minX;
                int ly = ny - minY;
                
                // On vérifie si la case a déjà été visitée ou si c'est un mur
                if (visited[lx][ly] || !level.isWalkableTile(nx, ny)) continue;
                
                // l'eau est interdite
                if (level.getFloorArray()[nx][ny] == 29) continue;

                visited[lx][ly] = true;
                parentX[lx][ly] = current[0];
                parentY[lx][ly] = current[1];
                queue.addLast(new int[]{nx, ny});
            }
        }

        if (!reachedTarget && bestTx == startTx && bestTy == startTy) {
            return new ArrayList<>();
        }

        List<int[]> result = new ArrayList<>();
        int tx = bestTx;
        int ty = bestTy;
        while (!(tx == startTx && ty == startTy)) {
            result.add(new int[]{tx, ty});
            int lx = tx - minX;
            int ly = ty - minY;
            int px = parentX[lx][ly];
            int py = parentY[lx][ly];
            if (px == -1 || py == -1) {
                return new ArrayList<>();
            }
            tx = px;
            ty = py;
        }

        Collections.reverse(result);
        return result;
    }

    private int distanceScore(int tx, int ty, int targetTx, int targetTy) {
        return Math.abs(tx - targetTx) + Math.abs(ty - targetTy);
    }

    private boolean isNearTile(int tx, int ty) {
        double targetX = tx * Config.blockSize + Config.blockSize / 2.0;
        double targetY = ty * Config.blockSize + Config.blockSize / 2.0;
        return Math.abs(getCenterX() - targetX) < 2.0
                && Math.abs(getCenterY() - targetY) < 2.0;
    }


    // --- MÉTHODES DE COMBAT ---
    public void takeDamage(int damage) {
        this.health -= damage;
        System.out.println("BAM ! Monstre touché. Dégâts : " + damage + " | Vie restante : " + this.health);
    }

    public boolean isDead() {
        return this.health <= 0;
    }
    
    protected boolean isTouchingPlayer(Player player) {
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

    public double getCenterX() {
        return x + Config.blockSize / 2.0;
    }

    public double getCenterY() {
        return y + Config.blockSize / 2.0;
    }

    protected int getTileX() {
        return (int) (getCenterX() / Config.blockSize);
    }

    protected int getTileY() {
        return (int) (getCenterY() / Config.blockSize);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
