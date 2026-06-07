package com.minicraft;

public class Config {
    public static final int blockSize = 16; // taille d'un block
    public static final int SCALE = 4;      // echelle de zoom
    public static final int itemSize = 6;   // taille d'un item

    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    public static final int PLAYER_MAX_HEALTH = 10;
    public static final int PLAYER_INVULNERABILITY_TICKS = 90;
    public static final int PLAYER_DAMAGE_BLINK_TICKS = 8;
    public static final double PLAYER_WALK_SPEED = 1.25;
    public static final double PLAYER_SPRINT_SPEED = 2.0;

    public static final int ENEMY_PATH_RECALC_TICKS = 30;
    public static final int ENEMY_PATH_RADIUS = 15;
    public static final int MELEE_BOT_COUNT = 10;
    public static final int ARCHER_BOT_COUNT = 4;
    public static final int ENEMY_SPAWN_ATTEMPTS = 500;
    public static final double ENEMY_MIN_PLAYER_SPAWN_DISTANCE = blockSize * 10.0;
    public static final double ENEMY_MAX_PLAYER_SPAWN_DISTANCE = blockSize * 45.0;
    public static final double ENEMY_MIN_BOT_SPAWN_DISTANCE = blockSize * 5.0;

    public static final double ARROW_SPEED = 2;
    public static final double ARROW_RADIUS = 2.0;
    public static final int ARROW_DAMAGE = 2;
    public static final double ARROW_MAX_DISTANCE = 260.0;

    public static final double ENEMY_BOT_SPEED = 1;
    public static final int ARCHER_SHOOT_COOLDOWN_TICKS = 600;
    public static final double ARCHER_SHOOT_RANGE = 220.0;
    public static final double ARCHER_KEEP_DISTANCE = blockSize * 6.0;

    public static final int SHEEP_COUNT = 40;
    public static final int SHEEP_NEAR_PLAYER_COUNT = 6;
    public static final double SHEEP_SPEED = 0.07;
    public static final int SHEEP_MIN_DIRECTION_TICKS = 60;
    public static final int SHEEP_MAX_DIRECTION_TICKS = 180;
    public static final double SHEEP_IDLE_CHANCE = 0.30;
    public static final int SHEEP_SPAWN_ATTEMPTS = 700;
    public static final double SHEEP_MIN_PLAYER_SPAWN_DISTANCE = blockSize * 5.0;
    public static final double SHEEP_NEAR_PLAYER_MAX_DISTANCE = blockSize * 55.0;
    public static final double SHEEP_MIN_ENTITY_SPAWN_DISTANCE = blockSize * 4.0;
}
