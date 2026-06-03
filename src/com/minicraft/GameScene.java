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
 * Cette classe gère l'écran de jeu avec le bouton style HUD.
 */
public class GameScene {
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

        // --- BOUTON STYLE HUD (Noir transparent comme Roche/Vie) ---
        Button btnBack = new Button("Menu principal (ESC)");
        styleButtonHUD(btnBack);

        btnBack.setOnAction(e -> returnToMenu());
        btnBack.setFocusTraversable(false);

        if (this.gameView != null) {
            this.root.getChildren().add(this.gameView);
        }

        StackPane.setAlignment(btnBack, Pos.TOP_RIGHT);
        StackPane.setMargin(btnBack, new Insets(10)); // Marge pour coller au bord
        this.root.getChildren().add(btnBack);
    }

    private void returnToMenu() {
        SaveManager.saveGame(gameEngine.getPlayer(), gameEngine);
        mainMenu.show(stage);
    }

    /**
     * Applique le style Noir Transparent (HUD)
     */
    private void styleButtonHUD(Button btn) {
        // Même couleur que : pinceau.setFill(Color.color(0, 0, 0, 0.5));
        String styleNormal =
                "-fx-background-color: rgba(0, 0, 0, 0.5); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 0; " + // Rectangulaire comme tes barres
                        "-fx-border-color: transparent; " +
                        "-fx-padding: 5 15;";

        // Un peu plus clair au survol pour savoir qu'on peut cliquer
        String styleHover =
                "-fx-background-color: rgba(0, 0, 0, 0.7); " +
                        "-fx-text-fill: #FFD700; " + // Texte doré au survol pour le retour visuel
                        "-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 0; " +
                        "-fx-padding: 5 15; " +
                        "-fx-cursor: hand;";

        btn.setStyle(styleNormal);
        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e -> btn.setStyle(styleNormal));
    }

    public void show() {
        stage.setScene(this.scene);
        stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                returnToMenu();
                event.consume();
            }
        });
        root.requestFocus();
    }

    public Main getGameEngine() {
        return this.gameEngine;
    }
}