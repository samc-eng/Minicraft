package com.minicraft;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

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
        private InventoryUI inventaireUI;
        private boolean inventaireOuvert = false;
        private CraftingUI craftingUI;
        private boolean craftingOpen = false;
        private boolean workbenchOpen = false;
        private HUD hud;
        private List<Bot> bots = new ArrayList<>();

    // Panneau principal qui contient le Canvas
    private Pane gameRoot;

    // ID du bloc Workbench dans ItemRegistry
    private static final int WORKBENCH_TILE_ID = 28;

    static public void main(String[] args) {
                launch(args);
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

    public Player getPlayer() { return this.player; }
        public Level getLevel() { return this.level; }

    public void launchGame(Stage primaryStage, Scene scene) {
                Canvas canva = new Canvas(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
                this.pinceau = canva.getGraphicsContext2D();
                this.level = new Level(4000, 4000);
                double[] spawn = level.getSafeSpawn();
                this.player = new Player(spawn[0], spawn[1]);
                for (int i = 0; i < 10; i++) {
                                this.bots.add(new Bot(spawn[0] + i * 40, spawn[1]));
                }

            this.gameRoot = new Pane();
                this.gameRoot.getChildren().add(canva);
                canva.widthProperty().bind(this.gameRoot.widthProperty());
                canva.heightProperty().bind(this.gameRoot.heightProperty());

            this.input = new InputHandler(scene);

            craftingUI = new CraftingUI();
                hud = new HUD();
                inventaireUI = new InventoryUI();

            // =============================================
            // RECETTES DE BASE (sans établi, touche C)
            // =============================================

            // Établi lui-même : 5 bois
            craftingUI.addRecipe(new Recipe("Etabli (Workbench)", 28, 1)
                                             .addCost(100, 5));

            // Torche : 1 bois + 1 charbon
            craftingUI.addRecipe(new Recipe("Torche x4", 27, 4)
                                             .addCost(100, 1)
                                             .addCost(102, 1));

            // Mur de bois : 5 bois
            craftingUI.addRecipe(new Recipe("Mur de bois", 10, 1)
                                             .addCost(100, 5));

            // Sol bois : 4 bois
            craftingUI.addRecipe(new Recipe("Sol de bois", 13, 1)
                                             .addCost(100, 4));

            // =============================================
            // RECETTES D'ÉTABLI (touche B quand près de l'établi)
            // =============================================

            // --- ÉPÉES ---
            craftingUI.addWorkbenchRecipe(new Recipe("Epee en bois", 200, 1)
                                                      .addCost(100, 2));

            craftingUI.addWorkbenchRecipe(new Recipe("Epee en pierre", 210, 1)
                                                      .addCost(101, 3)
                                                      .addCost(100, 1));

            craftingUI.addWorkbenchRecipe(new Recipe("Epee en fer", 220, 1)
                                                      .addCost(105, 3)
                                                      .addCost(100, 1));

            craftingUI.addWorkbenchRecipe(new Recipe("Epee en or", 230, 1)
                                                      .addCost(106, 3)
                                                      .addCost(100, 1));

            craftingUI.addWorkbenchRecipe(new Recipe("Epee en gem", 240, 1)
                                                      .addCost(107, 3)
                                                      .addCost(100, 1));

            // --- PIOCHES ---
            craftingUI.addWorkbenchRecipe(new Recipe("Pioche en bois", 202, 1)
                                                      .addCost(100, 3));

            craftingUI.addWorkbenchRecipe(new Recipe("Pioche en pierre", 212, 1)
                                                      .addCost(101, 3)
                                                      .addCost(100, 2));

            craftingUI.addWorkbenchRecipe(new Recipe("Pioche en fer", 222, 1)
                                                      .addCost(105, 3)
                                                      .addCost(100, 2));

            craftingUI.addWorkbenchRecipe(new Recipe("Pioche en or", 232, 1)
                                                      .addCost(106, 3)
                                                      .addCost(100, 2));

            craftingUI.addWorkbenchRecipe(new Recipe("Pioche en gem", 242, 1)
                                                      .addCost(107, 3)
                                                      .addCost(100, 2));

            // --- HACHES ---
            craftingUI.addWorkbenchRecipe(new Recipe("Hache en bois", 201, 1)
                                                      .addCost(100, 3));

            craftingUI.addWorkbenchRecipe(new Recipe("Hache en pierre", 211, 1)
                                                      .addCost(101, 3)
                                                      .addCost(100, 2));

            craftingUI.addWorkbenchRecipe(new Recipe("Hache en fer", 221, 1)
                                                      .addCost(105, 3)
                                                      .addCost(100, 2));

            craftingUI.addWorkbenchRecipe(new Recipe("Hache en or", 231, 1)
                                                      .addCost(106, 3)
                                                      .addCost(100, 2));

            craftingUI.addWorkbenchRecipe(new Recipe("Hache en gem", 241, 1)
                                                      .addCost(107, 3)
                                                      .addCost(100, 2));

            // --- PELLES ---
            craftingUI.addWorkbenchRecipe(new Recipe("Pelle en bois", 203, 1)
                                                      .addCost(100, 2));

            craftingUI.addWorkbenchRecipe(new Recipe("Pelle en pierre", 213, 1)
                                                      .addCost(101, 2)
                                                      .addCost(100, 2));

            craftingUI.addWorkbenchRecipe(new Recipe("Pelle en fer", 223, 1)
                                                      .addCost(105, 2)
                                                      .addCost(100, 2));

            // --- ARCS ---
            craftingUI.addWorkbenchRecipe(new Recipe("Arc en bois", 205, 1)
                                                      .addCost(100, 3)
                                                      .addCost(118, 3));

            craftingUI.addWorkbenchRecipe(new Recipe("Arc en pierre", 215, 1)
                                                      .addCost(101, 2)
                                                      .addCost(118, 3));

            craftingUI.addWorkbenchRecipe(new Recipe("Arc en fer", 225, 1)
                                                      .addCost(105, 2)
                                                      .addCost(118, 3));

            // --- CONSTRUCTIONS ---
            craftingUI.addWorkbenchRecipe(new Recipe("Mur de pierre x4", 9, 4)
                                                      .addCost(101, 4));

            craftingUI.addWorkbenchRecipe(new Recipe("Mur d'obsidienne", 11, 1)
                                                      .addCost(109, 4));

            craftingUI.addWorkbenchRecipe(new Recipe("Sol de pierre", 12, 1)
                                                      .addCost(101, 3));

            craftingUI.addWorkbenchRecipe(new Recipe("Lingot de fer", 105, 1)
                                                      .addCost(103, 2)
                                                      .addCost(102, 1));

            craftingUI.addWorkbenchRecipe(new Recipe("Lingot d'or", 106, 1)
                                                      .addCost(104, 2)
                                                      .addCost(102, 1));

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
                                                if (camX > (level.getWidth() * Config.blockSize) - largeurVue) camX = (level.getWidth() * Config.blockSize) - largeurVue;
                                                if (camY > (level.getHeight() * Config.blockSize) - hauteurVue) camY = (level.getHeight() * Config.blockSize) - hauteurVue;

                                // --- Gestion inventaire ---
                                if (input.isClicked(KeyCode.E)) {
                                                        inventaireOuvert = !inventaireOuvert;
                                                        craftingOpen = false;
                                                        workbenchOpen = false;
                                }

                                // --- Gestion craft de base (touche C) ---
                                if (input.isClicked(KeyCode.C)) {
                                                        craftingOpen = !craftingOpen;
                                                        workbenchOpen = false;
                                                        inventaireOuvert = false;
                                                        craftingUI.setModeEtabli(false);
                                }

                                // --- Gestion établi (touche B) : seulement si près d'un établi ---
                                if (input.isClicked(KeyCode.B)) {
                                                        boolean pres = isNearWorkbench();
                                                        if (pres) {
                                                                                    workbenchOpen = !workbenchOpen;
                                                                                    craftingOpen = false;
                                                                                    inventaireOuvert = false;
                                                                                    craftingUI.setModeEtabli(workbenchOpen);
                                                        } else {
                                                                                    System.out.println("Vous devez etre pres d'un etabli !");
                                                        }
                                }

                                pinceau.clearRect(0, 0, widthScreen, heightScreen);

                                pinceau.save();
                                                pinceau.scale(Config.SCALE, Config.SCALE);

                                level.render(pinceau, camX, camY, largeurVue, hauteurVue);

                                for (Bot bot : bots) {
                                                        bot.tick(level, player);
                                                        bot.render(pinceau, camX, camY);
                                }

                                player.tick(level, input);
                                                player.render(pinceau);

                                hud.render(pinceau, player, widthScreen / Config.SCALE, heightScreen / Config.SCALE);

                                if (inventaireOuvert) {
                                                        inventaireUI.render(pinceau, player.getInventory());
                                }

                                if (craftingOpen || workbenchOpen) {
                                                        craftingUI.tick(input, player.getInventory());
                                                        craftingUI.render(pinceau, player.getInventory());
                                }

                                pinceau.restore();
                                                input.update();
                            }
            };
                timer.start();
    }

    /**
     * Vérifie si le joueur est adjacent (dans les 2 cases) d'un bloc Workbench.
         */
    private boolean isNearWorkbench() {
                int px = (int)(player.getX() / Config.blockSize);
                int py = (int)(player.getY() / Config.blockSize);
                for (int dx = -2; dx <= 2; dx++) {
                                for (int dy = -2; dy <= 2; dy++) {
                                                    int tile = level.getTile(px + dx, py + dy);
                                                    if (tile == WORKBENCH_TILE_ID) {
                                                                            return true;
                                                    }
                                }
                }
                return false;
    }
}
