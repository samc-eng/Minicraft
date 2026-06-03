package com.minicraft;

public class Config {
    public static final int blockSize = 16;//taile d'un block
    public static final int SCALE = 4; //échelle de zoom
    public static final int itemSize = 6; //taile d'un item
    
    //taile de la fenêtre au lancement
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    // Paramètres de l'IA des ennemis
    public static final int ENEMY_PATH_RECALC_TICKS = 30; // Le bot recalcule son chemin toutes les 30 frames
    public static final int ENEMY_PATH_RADIUS = 15;       // Le bot peut voir à 15 blocs de distance maximum

    // Paramètres des projectiles (Flèches)
    public static final double ARROW_SPEED = 4.0;  // Vitesse de vol de la flèche
    public static final double ARROW_RADIUS = 2.0; // Taille de la hitbox de la flèche
    public static final int ARROW_DAMAGE = 2;      // Nombre de cœurs perdus par le joueur

    // Paramètres de l'ArcherBot
    public static final double ENEMY_BOT_SPEED = 1.2;            // Vitesse de déplacement du bot
    public static final int ARCHER_SHOOT_COOLDOWN_TICKS = 90;    // Temps d'attente entre deux flèches (ex: 90 frames = 1.5 seconde)
    public static final double ARCHER_SHOOT_RANGE = 200.0;       // Portée de vision et de tir (en pixels)
}
