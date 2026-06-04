package com.minicraft;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

public class Main extends Application {
    private Player player;
    private GraphicsContext pinceau;
    private Level level;
    private InputHandler input;
    private double widthScreen;
    private double heightScreen;
    private CraftingUI craftingUI;
    private boolean craftingOpen  = false;
    private boolean workbenchOpen = false;
    private InventoryUI inventaireUI;
    private boolean inventaireOuvert = false;
    private HUD hud;
    private MiniMap miniMap;
    private boolean miniMapVisible = true;
    private boolean fullMapOpen    = false;

    // Gestion multi-niveaux
    private Level   surfaceLevel;
    private Level[] undergroundLevels  = new Level[5];
    private int     currentDepth       = 0;
    private int     transitionCooldown = 0;
    private int[]   lastSurfacePortal  = null;

    private Pane gameRoot;

    private static final int WORKBENCH_TILE_ID = 28;

    static public void main(String[] args) {
        launch(args);
    }

    private boolean hasInHotbar(int itemId) {
        for (ItemStack s : player.getHotbar()) {
            if (s != null && s.getItemId() == itemId) return true;
        }
        return false;
    }

    @Override
    public void start(Stage primaryStage) {
        MainMenu menu = new MainMenu();
        menu.show(primaryStage);
    }

    public Pane getGameView(Stage stage, Scene scene) {
        if (this.gameRoot == null) {
            this.launchGame(stage, scene);
        }
        return this.gameRoot;
    }

    public Player getPlayer()   { return this.player; }
    public Level getLevel()     { return this.level; }
    public MiniMap getMiniMap() { return this.miniMap; }

    public Level   getSurfaceLevel()         { return this.surfaceLevel; }
    public Level[] getUndergroundLevels()    { return this.undergroundLevels; }
    public int     getCurrentDepth()         { return this.currentDepth; }
    public int[]   getLastSurfacePortal()    { return this.lastSurfacePortal; }

    // Restaure l'état complet du jeu depuis une sauvegarde.
    // - surface : niveau de surface (toujours présent)
    // - cave    : grotte visitée (null si jamais entré)
    // - depth   : où le joueur se trouve (0 surface, >=1 grotte)
    // - lastPortal : portail par lequel le joueur est descendu (null sinon)
    public void loadGameState(Level surface, Level cave, int depth, int[] lastPortal) {
        this.surfaceLevel = surface;
        if (cave != null) {
            int idx = cave.getDepth() - 1;
            if (idx >= 0 && idx < this.undergroundLevels.length) {
                this.undergroundLevels[idx] = cave;
            }
        }
        this.currentDepth = depth;
        this.lastSurfacePortal = lastPortal;
        this.level = (depth == 0 || cave == null) ? surface : cave;
        this.miniMap.generate(this.level.getFloorArray(), this.level.getBlocksArray(),
                              this.level.getWidth(), this.level.getHeight(),
                              this.level.getPortals());
    }

    private void spawnNearPortal(Level targetLevel) {
        if (targetLevel.getPortals().isEmpty()) {
            double[] s = targetLevel.getSafeSpawn();
            player.setX(s[0]); player.setY(s[1]);
            return;
        }
        int[] p = targetLevel.getPortals().get(0);
        int[][] offsets = {{0,1},{0,-1},{1,0},{-1,0},{1,1},{-1,1},{1,-1},{-1,-1}};
        for (int[] off : offsets) {
            double tx = (p[0] + off[0]) * Config.blockSize;
            double ty = (p[1] + off[1]) * Config.blockSize;
            if (!targetLevel.isSolid(tx, ty)) {
                player.setX(tx);
                player.setY(ty);
                return;
            }
        }
        double[] s = targetLevel.getSafeSpawn();
        player.setX(s[0]); player.setY(s[1]);
    }

    private boolean isNearWorkbench() {
        int px = (int)(player.getX() / Config.blockSize);
        int py = (int)(player.getY() / Config.blockSize);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (level.getTile(px + dx, py + dy) == WORKBENCH_TILE_ID) return true;
            }
        }
        return false;
    }

    public void clearEnemiesForCurrentLevel() {
        if (level != null) {
            level.clearEnemies();
        }
    }

    public void spawnEnemiesForCurrentLevel() {
        if (level == null || player == null || currentDepth != 0) {
            return;
        }

        level.clearEnemies();
        List<double[]> reservedPositions = new ArrayList<>();

        for (int i = 0; i < Config.MELEE_BOT_COUNT; i++) {
            double[] spawn = level.findEnemySpawnAround(player.getCenterX(), player.getCenterY(), reservedPositions);
            if (spawn != null) {
                level.addBot(spawn[0], spawn[1]);
                reservedPositions.add(spawn);
            }
        }

        for (int i = 0; i < Config.ARCHER_BOT_COUNT; i++) {
            double[] spawn = level.findEnemySpawnAround(player.getCenterX(), player.getCenterY(), reservedPositions);
            if (spawn != null) {
                level.addArcherBot(spawn[0], spawn[1]);
                reservedPositions.add(spawn);
            }
        }
    }

    public void spawnMissingArchersForCurrentLevel() {
        if (level == null || player == null || currentDepth != 0) {
            return;
        }

        List<double[]> reservedPositions = new ArrayList<>();
        for (Bot bot : level.getBots()) {
            reservedPositions.add(new double[]{bot.getX(), bot.getY()});
        }
        for (ArcherBot archer : level.getArcherBots()) {
            reservedPositions.add(new double[]{archer.getX(), archer.getY()});
        }

        int missingArchers = Config.ARCHER_BOT_COUNT - level.getArcherBots().size();
        for (int i = 0; i < missingArchers; i++) {
            double[] spawn = level.findEnemySpawnAround(player.getCenterX(), player.getCenterY(), reservedPositions);
            if (spawn != null) {
                level.addArcherBot(spawn[0], spawn[1]);
                reservedPositions.add(spawn);
            }
        }
    }

    public void launchGame(Stage primaryStage, Scene scene) {
        Canvas canva = new Canvas(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        this.pinceau = canva.getGraphicsContext2D();
        this.surfaceLevel = new Level(4000, 4000);
        this.level        = this.surfaceLevel;

        // Écouteur pour la molette de la souris
    scene.setOnScroll(event -> {
        int currentSlot = player.getSelectedSlot();
        
        if (event.getDeltaY() > 0) {
            // Molette vers le haut (on va vers la gauche)
            currentSlot--;
            if (currentSlot < 0) currentSlot = 8; // Boucle à la fin
        } else if (event.getDeltaY() < 0) {
            // Molette vers le bas (on va vers la droite)
            currentSlot++;
            if (currentSlot > 8) currentSlot = 0; // Boucle au début
        }
        
        // On met à jour le slot du joueur (Il faut rendre ta méthode setSelectedSlot 'public' !)
        player.setSelectedSlot(currentSlot); 
    });

        double[] spawn = level.getSafeSpawn();
        this.player = new Player(spawn[0], spawn[1]);
        spawnEnemiesForCurrentLevel();

        this.gameRoot = new Pane();
        this.gameRoot.getChildren().add(canva);
        canva.widthProperty().bind(this.gameRoot.widthProperty());
        canva.heightProperty().bind(this.gameRoot.heightProperty());

        this.input = new InputHandler(scene);

        miniMap = new MiniMap();
        miniMap.generate(level.getFloorArray(), level.getBlocksArray(),
                         level.getWidth(), level.getHeight(), level.getPortals());

        craftingUI   = new CraftingUI();
        hud          = new HUD();
        inventaireUI = new InventoryUI();

        scene.setOnMouseClicked(event -> {
            if (!inventaireOuvert) return;
            inventaireUI.handleClick(player, event.getX(), event.getY());
        });

        // =============================================
        // RECETTES DE BASE (touche C)
        // =============================================
        craftingUI.addRecipe(new Recipe("Etabli (Workbench)", 28, 1).addCost(100, 5));
        craftingUI.addRecipe(new Recipe("Torche x4",          27, 4).addCost(100, 1).addCost(102, 1));
        craftingUI.addRecipe(new Recipe("Mur de bois",        10, 1).addCost(100, 5));
        craftingUI.addRecipe(new Recipe("Sol de bois",        13, 1).addCost(100, 4));

        // =============================================
        // RECETTES D'ÉTABLI (touche B)
        // =============================================
        // Épées
        craftingUI.addWorkbenchRecipe(new Recipe("Epee en bois",   200, 1).addCost(100, 2));
        craftingUI.addWorkbenchRecipe(new Recipe("Epee en pierre", 210, 1).addCost(101, 3).addCost(100, 1));
        craftingUI.addWorkbenchRecipe(new Recipe("Epee en fer",    220, 1).addCost(105, 3).addCost(100, 1));
        craftingUI.addWorkbenchRecipe(new Recipe("Epee en or",     230, 1).addCost(106, 3).addCost(100, 1));
        craftingUI.addWorkbenchRecipe(new Recipe("Epee en gem",    240, 1).addCost(107, 3).addCost(100, 1));

        // Pioches
        craftingUI.addWorkbenchRecipe(new Recipe("Pioche en bois",   202, 1).addCost(100, 3));
        craftingUI.addWorkbenchRecipe(new Recipe("Pioche en pierre", 212, 1).addCost(101, 3).addCost(100, 2));
        craftingUI.addWorkbenchRecipe(new Recipe("Pioche en fer",    222, 1).addCost(105, 3).addCost(100, 2));
        craftingUI.addWorkbenchRecipe(new Recipe("Pioche en or",     232, 1).addCost(106, 3).addCost(100, 2));
        craftingUI.addWorkbenchRecipe(new Recipe("Pioche en gem",    242, 1).addCost(107, 3).addCost(100, 2));

        // Haches
        craftingUI.addWorkbenchRecipe(new Recipe("Hache en bois",   201, 1).addCost(100, 3));
        craftingUI.addWorkbenchRecipe(new Recipe("Hache en pierre", 211, 1).addCost(101, 3).addCost(100, 2));
        craftingUI.addWorkbenchRecipe(new Recipe("Hache en fer",    221, 1).addCost(105, 3).addCost(100, 2));
        craftingUI.addWorkbenchRecipe(new Recipe("Hache en or",     231, 1).addCost(106, 3).addCost(100, 2));
        craftingUI.addWorkbenchRecipe(new Recipe("Hache en gem",    241, 1).addCost(107, 3).addCost(100, 2));

        // Pelles
        craftingUI.addWorkbenchRecipe(new Recipe("Pelle en bois",   203, 1).addCost(100, 2));
        craftingUI.addWorkbenchRecipe(new Recipe("Pelle en pierre", 213, 1).addCost(101, 2).addCost(100, 2));
        craftingUI.addWorkbenchRecipe(new Recipe("Pelle en fer",    223, 1).addCost(105, 2).addCost(100, 2));

        // Arcs
        craftingUI.addWorkbenchRecipe(new Recipe("Arc en bois",   205, 1).addCost(100, 3).addCost(118, 3));
        craftingUI.addWorkbenchRecipe(new Recipe("Arc en pierre", 215, 1).addCost(101, 2).addCost(118, 3));
        craftingUI.addWorkbenchRecipe(new Recipe("Arc en fer",    225, 1).addCost(105, 2).addCost(118, 3));

        // Constructions
        craftingUI.addWorkbenchRecipe(new Recipe("Mur de pierre x4", 9,   4).addCost(101, 4));
        craftingUI.addWorkbenchRecipe(new Recipe("Mur d'obsidienne", 11,  1).addCost(109, 4));
        craftingUI.addWorkbenchRecipe(new Recipe("Sol de pierre",    12,  1).addCost(101, 3));
        craftingUI.addWorkbenchRecipe(new Recipe("Lingot de fer",    105, 1).addCost(103, 2).addCost(102, 1));
        craftingUI.addWorkbenchRecipe(new Recipe("Lingot d'or",      106, 1).addCost(104, 2).addCost(102, 1));

        // Armures en cuir
        craftingUI.addWorkbenchRecipe(new Recipe("Casque en cuir",    300, 1).addCost(100, 5));
        craftingUI.addWorkbenchRecipe(new Recipe("Plastron en cuir",  301, 1).addCost(100, 8));
        craftingUI.addWorkbenchRecipe(new Recipe("Jambieres en cuir", 302, 1).addCost(100, 7));
        craftingUI.addWorkbenchRecipe(new Recipe("Bottes en cuir",    303, 1).addCost(100, 4));

        // Armures en fer
        craftingUI.addWorkbenchRecipe(new Recipe("Casque en fer",    310, 1).addCost(105, 5));
        craftingUI.addWorkbenchRecipe(new Recipe("Plastron en fer",  311, 1).addCost(105, 8));
        craftingUI.addWorkbenchRecipe(new Recipe("Jambieres en fer", 312, 1).addCost(105, 7));
        craftingUI.addWorkbenchRecipe(new Recipe("Bottes en fer",    313, 1).addCost(105, 4));

        // Armures en or
        craftingUI.addWorkbenchRecipe(new Recipe("Casque en or",    320, 1).addCost(106, 5));
        craftingUI.addWorkbenchRecipe(new Recipe("Plastron en or",  321, 1).addCost(106, 8));
        craftingUI.addWorkbenchRecipe(new Recipe("Jambieres en or", 322, 1).addCost(106, 7));
        craftingUI.addWorkbenchRecipe(new Recipe("Bottes en or",    323, 1).addCost(106, 4));

        // Armures en gem
        craftingUI.addWorkbenchRecipe(new Recipe("Casque en gem",    330, 1).addCost(107, 5));
        craftingUI.addWorkbenchRecipe(new Recipe("Plastron en gem",  331, 1).addCost(107, 8));
        craftingUI.addWorkbenchRecipe(new Recipe("Jambieres en gem", 332, 1).addCost(107, 7));
        craftingUI.addWorkbenchRecipe(new Recipe("Bottes en gem",    333, 1).addCost(107, 4));

        AnimationTimer timer = new AnimationTimer() {
            public void handle(long now) {
                widthScreen = canva.getWidth();
                heightScreen = canva.getHeight();

                double largeurVue = widthScreen / Config.SCALE;
                double hauteurVue = heightScreen / Config.SCALE;

                double camX = player.getX() + Config.blockSize / 2 - largeurVue / 2;
                double camY = player.getY() + Config.blockSize / 2 - hauteurVue / 2;

                if (camX < 0) camX = 0;
                if (camY < 0) camY = 0;
                if (camX > (level.getWidth()  * Config.blockSize) - largeurVue) camX = (level.getWidth()  * Config.blockSize) - largeurVue;
                if (camY > (level.getHeight() * Config.blockSize) - hauteurVue) camY = (level.getHeight() * Config.blockSize) - hauteurVue;

                // --- Transitions entre niveaux ---
                if (transitionCooldown > 0) {
                    transitionCooldown--;
                } else {
                    int ptx = (int)(player.getX() / Config.blockSize);
                    int pty = (int)(player.getY() / Config.blockSize);
                    for (int[] portal : level.getPortals()) {
                        if (Math.abs(ptx - portal[0]) <= 1 && Math.abs(pty - portal[1]) <= 1) {
                            if (currentDepth == 0) {
                                lastSurfacePortal = portal;
                                currentDepth = 1;
                                if (undergroundLevels[0] == null) {
                                    undergroundLevels[0] = new Level(1000, 1000, 1, surfaceLevel.getSeed());
                                }
                                level = undergroundLevels[0];
                                spawnNearPortal(level);
                            } else {
                                currentDepth = 0;
                                level = surfaceLevel;
                                if (lastSurfacePortal != null) {
                                    player.setX(lastSurfacePortal[0] * Config.blockSize + Config.blockSize);
                                    player.setY(lastSurfacePortal[1] * Config.blockSize + Config.blockSize);
                                } else {
                                    double[] s = level.getSafeSpawn();
                                    player.setX(s[0]); player.setY(s[1]);
                                }
                            }
                            miniMap.generate(level.getFloorArray(), level.getBlocksArray(),
                                             level.getWidth(), level.getHeight(), level.getPortals());
                            transitionCooldown = 90;
                            break;
                        }
                    }
                }

                // --- Touches UI ---
                if (input.isClicked(KeyCode.M))   miniMapVisible = !miniMapVisible;
                if (input.isClicked(KeyCode.TAB)) fullMapOpen    = !fullMapOpen;

                if (input.isClicked(KeyCode.E)) {
                    inventaireOuvert = !inventaireOuvert;
                    craftingOpen  = false;
                    workbenchOpen = false;
                }

                if (input.isClicked(KeyCode.C)) {
                    craftingOpen     = !craftingOpen;
                    workbenchOpen    = false;
                    inventaireOuvert = false;
                    craftingUI.setModeEtabli(false);
                }

                if (input.isClicked(KeyCode.B)) {
                    if (isNearWorkbench()) {
                        workbenchOpen    = !workbenchOpen;
                        craftingOpen     = false;
                        inventaireOuvert = false;
                        craftingUI.setModeEtabli(workbenchOpen);
                    } else {
                        System.out.println("Vous devez être près d'un établi !");
                    }
                }

                // --- Rendu ---
                pinceau.clearRect(0, 0, widthScreen, heightScreen);
                pinceau.save();
                pinceau.scale(Config.SCALE, Config.SCALE);
                pinceau.translate(-camX, -camY);

                level.render(pinceau, camX, camY, largeurVue, hauteurVue);

                // --- Tick ---
                if (craftingOpen || workbenchOpen || inventaireOuvert) {
                    // On ne bouge pas le joueur quand un menu est ouvert
                } else {
                    player.tick(level, input);
                    level.updateEntities(player);
                }

                player.render(pinceau);
                pinceau.restore();

                // --- HUD & UI ---
                hud.render(pinceau, player);

                if (craftingOpen || workbenchOpen) {
                    craftingUI.tick(input, player);
                    craftingUI.render(pinceau, player);
                } else if (inventaireOuvert) {
                    inventaireUI.render(pinceau, player);
                }

                // --- Minimap ---
                if (fullMapOpen) {
                    miniMap.renderFullscreen(pinceau,
                        player.getX(), player.getY(),
                        camX, camY, largeurVue, hauteurVue,
                        widthScreen, heightScreen);
                } else if (miniMapVisible) {
                    miniMap.render(pinceau,
                        player.getX(), player.getY(),
                        camX, camY, largeurVue, hauteurVue,
                        widthScreen, heightScreen);
                }

                input.update();
            }
        };

        timer.start();
    }
}
