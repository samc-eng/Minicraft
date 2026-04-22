package com.minicraft;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Cette classe gère l'écran de jeu.
 */
public class GameScene {
    // --- VARIABLES DE CLASSE ---
    private MainMenu mainMenu;
    private Stage stage;
    private Scene scene;
    private Pane gameView;
    private Main gameEngine;
    private StackPane root;

    public GameScene(Stage stage, MainMenu mainMenu) {
        this.stage = stage;
        this.mainMenu = mainMenu;
        this.root = new StackPane();
        this.scene = new Scene(this.root, 1200, 800);

        this.gameEngine = new Main();
        this.gameView = gameEngine.getGameView(stage, this.scene);

        Button btnBack = new Button("Menu principal (ESC)");
        styleButton(btnBack);

        btnBack.setOnAction(e -> returnToMenu());
        btnBack.setFocusTraversable(false);

        if (this.gameView != null) {
            this.root.getChildren().add(this.gameView);
        }

        StackPane.setAlignment(btnBack, Pos.TOP_RIGHT);
        StackPane.setMargin(btnBack, new Insets(15));
        this.root.getChildren().add(btnBack);
    }

    /**
     * Méthode corrigée : On a enlevé l'accolade qui fermait trop tôt !
     */
    private void returnToMenu() {
        System.out.println(">>> ACTION : Sauvegarde effectuée et retour au menu <<<");

        // On appelle la sauvegarde en passant le joueur ET le niveau
        SaveManager.saveGame(gameEngine.getPlayer(), gameEngine.getLevel());

        // On retourne au menu
        mainMenu.show(stage);
    }

    private void styleButton(Button btn) {
        String styleNormal = "-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;";
        String styleHover = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;";

        btn.setStyle(styleNormal);
        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e -> btn.setStyle(styleNormal));
    }

    public void show() {
        stage.setScene(this.scene);

        stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                System.out.println(">>> TOUCHE ESC DETECTÉE <<<");
                returnToMenu();
                event.consume();
            }
        });

        root.requestFocus();
        if (gameView != null) {
            gameView.requestFocus();
        }
    }

    public Main getGameEngine() {
        return this.gameEngine;
    }
}