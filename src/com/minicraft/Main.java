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
    private GraphicsContext gc; // Ajouté si nécessaire pour certains rendus
    private CraftingUI craftingUI;
    private boolean craftingOpen = false;
    private List<Bot> bots = new ArrayList<>();

    // Panneau principal qui contient le Canvas
    private Pane gameRoot;

    static public void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MainMenu menu = new MainMenu();
        menu.show(primaryStage);
    }

    // MODIFICATION : On demande la Scene en paramètre
    public Pane getGameView(Stage stage, Scene scene) {
        if (this.gameRoot == null) {
            this.launchGame(stage, scene);
        }
        return this.gameRoot;
    }

    /**
     * GETTER : Permet d'accéder au joueur.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * GETTER : Permet d'accéder au niveau (Ajouté pour la sauvegarde).
     */
    public Level getLevel() {
        return this.level;
    }

    // MODIFICATION : On reçoit la Scene pour initialiser l'InputHandler proprement
    public void launchGame(Stage primaryStage, Scene scene) {
        Canvas canva = new Canvas(Config.SCREEN_WIDTH, Config.SCREEN_HEIGHT);
        this.pinceau = canva.getGraphicsContext2D();
        this.player = new Player(50, 50);
        this.level = new Level(4000, 4000);
        for (int i=0; i<15; i++){
            this.bots.add(new Bot(200 + i*40,250));
        }

        this.gameRoot = new Pane();
        this.gameRoot.getChildren().add(canva);

        canva.widthProperty().bind(this.gameRoot.widthProperty());
        canva.heightProperty().bind(this.gameRoot.heightProperty());

        // --- CLAVIER ---
        // On branche l'InputHandler sur la scène fournie par GameScene
        this.input = new InputHandler(scene);

        craftingUI = new CraftingUI();
        craftingUI.addRecipe(new Recipe("Mur de pierre", 3, 1).addCost(1, 5));
        inventaireUI = new InventoryUI();

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

                if (input.isClicked(KeyCode.E)) {
                    inventaireOuvert = !inventaireOuvert;
                    craftingOpen = false;
                }

                if (input.isClicked(KeyCode.C)) {
                    craftingOpen = !craftingOpen;
                    inventaireOuvert = false;
                }

                pinceau.clearRect(0, 0, widthScreen, heightScreen);
                pinceau.save();
                pinceau.scale(Config.SCALE, Config.SCALE);
                pinceau.translate(-camX, -camY);

                level.render(pinceau, camX, camY, largeurVue, hauteurVue);
                for (Bot bot : bots) {
                    bot.render(pinceau);
                }

                if (inventaireOuvert) {
                    // Logique inventaire
                } else if (craftingOpen) {
                    craftingUI.tick(input, player.getInventory());
                } else {
                    player.tick(level, input);
                    for (Bot bot : bots) {
                        bot.tick(level, player);
                    }
                    level.updateEntities(player);
                }

                player.render(pinceau);
                pinceau.restore();

                if (craftingOpen) {
                    craftingUI.render(pinceau, player.getInventory());
                } else if (inventaireOuvert) {
                    inventaireUI.render(pinceau, player);
                } else {
                    pinceau.setFill(Color.color(0, 0, 0, 0.5));
                    pinceau.fillRect(10, 10, 120, 25); // Fond pour la roche
                    pinceau.setFill(Color.WHITE);
                    pinceau.setFont(javafx.scene.text.Font.font("Arial", 14));
                    pinceau.fillText("Roche : " + player.getInventory().getAmount(1), 20, 28);

                    // --- Affichage VIE ---
                    pinceau.setFill(Color.color(0, 0, 0, 0.5));
                    pinceau.fillRect(10, 40, 120, 25); // Fond identique pour la vie (décalé vers le bas)
                    pinceau.setFill(Color.WHITE);
                    pinceau.fillText("Vie : " + player.getHealth(), 20, 58);

                }
                input.update();
            }
        };

        timer.start();
    }
}
