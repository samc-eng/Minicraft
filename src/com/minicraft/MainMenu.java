package com.minicraft;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.media.*;
import java.util.*;

public class MainMenu {
    private MediaPlayer mediaPlayer;
    int largeurBouton = 200; // Retour à ta taille originale

    String styleBouton =
            "-fx-background-color: linear-gradient(#5dade2, #2e86c1); " +
                    "-fx-background-radius: 30; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-family: 'Arial Rounded MT Bold'; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 18px; " + // Retour à tes 18px
                    "-fx-padding: 10 20 10 20; " + // Retour à ton épaisseur
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 4);";

    public void show(Stage window) {
        // ... (Musique et Fond identiques)
        try {
            Media hit = new Media(new java.io.File("resources/menu_music.mp3").toURI().toString());
            this.mediaPlayer = new MediaPlayer(hit);
            this.mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            this.mediaPlayer.setVolume(0.5);
            this.mediaPlayer.play();
        } catch (Exception e) { }

        StackPane root = new StackPane();

        try {
        java.io.File fichier = new java.io.File("resources/bg.png");
        if (fichier.exists()) {
            javafx.scene.image.Image backgroundImage = new javafx.scene.image.Image(fichier.toURI().toString());
            javafx.scene.image.ImageView backgroundView = new javafx.scene.image.ImageView(backgroundImage);
            backgroundView.fitWidthProperty().bind(window.widthProperty());
            backgroundView.fitHeightProperty().bind(window.heightProperty());
            backgroundView.setPreserveRatio(false);
            root.getChildren().add(0, backgroundView);
        }
        } catch (Exception e) { }

        Text title = new Text("MINICRAFT");
        title.setFont(Font.font("VERDANA", FontWeight.BOLD, 100));
        title.setFill(Color.GOLD);
        title.setStroke(Color.BROWN);
        title.setStrokeWidth(3);

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2), title);
        pulse.setFromX(0.8); pulse.setFromY(0.8);
        pulse.setToX(1.1); pulse.setToY(1.1);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        VBox menuBox = new VBox(40);
        menuBox.setAlignment(Pos.CENTER);

        // Création des boutons avec ton style
        Button btnNewGame = createStyledButton("Créer un nouveau monde");
        btnNewGame.setOnAction(e -> startNewGame(window));

        Button btnLoadGame = createStyledButton("Continuer la partie");
        btnLoadGame.setOnAction(e -> loadExistingGame(window));

        Button btnInfos = createStyledButton("Informations");

        Button btnQuitter = createStyledButton("Quitter");
        btnQuitter.setOnAction(e -> window.close());

        menuBox.getChildren().addAll(title, btnNewGame, btnLoadGame, btnInfos, btnQuitter);
        root.getChildren().add(menuBox);

        Scene menuScene = new Scene(root, 1000, 700);
        window.setScene(menuScene);

        // On demande le focus sur le root pour virer le contour bleu sur le bouton 1
        root.requestFocus();

        window.show();
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setMinWidth(largeurBouton);

        // C'est cette ligne qui empêche le contour bleu de focus
        btn.setFocusTraversable(false);

        // On remet tes réglages : neutre au départ, style au survol
        btn.setStyle(null);
        btn.setOnMouseEntered(e -> btn.setStyle(styleBouton + "-fx-scale-x: 1.1; -fx-scale-y: 1.1;"));
        btn.setOnMouseExited(e -> btn.setStyle(null));

        return btn;
    }

    private void startNewGame(Stage window) {
        if (mediaPlayer != null) mediaPlayer.stop();
        GameScene gameScene = new GameScene(window, this);
        gameScene.show();
    }

    private void loadExistingGame(Stage window) {
        Scanner sc = SaveManager.getSaveScanner();
        if (sc == null) return;
        try {
            if (mediaPlayer != null) mediaPlayer.stop();
            // 1. Lire les données du joueur
            String[] pData = sc.nextLine().split(",");

            // 2. Créer le moteur et la scène
            GameScene gameScene = new GameScene(window, this);
            Level lvl = gameScene.getGameEngine().getLevel();

            // 3. Lire les dimensions et les maps
            String[] dimensions = sc.nextLine().split(",");
            int w = Integer.parseInt(dimensions[0]);
            int h = Integer.parseInt(dimensions[1]);

            // Charger le sol
            String[] floorData = sc.nextLine().split(",");
            int[][] newFloor = new int[w][h];
            int idx = 0;
            for(int i=0; i<w; i++) for(int j=0; j<h; j++) newFloor[i][j] = Integer.parseInt(floorData[idx++]);

            // Charger les blocs (Arbres/Roches)
            String[] blocksData = sc.nextLine().split(",");
            int[][] newBlocks = new int[w][h];
            idx = 0;
            for(int i=0; i<w; i++) for(int j=0; j<h; j++) newBlocks[i][j] = Integer.parseInt(blocksData[idx++]);

            // 4. Appliquer tout au jeu
            lvl.setFloorArray(newFloor);
            lvl.setBlocksArray(newBlocks);

            // 5. Régénérer la minimap avec les données chargées
            gameScene.getGameEngine().getMiniMap().generate(newFloor, newBlocks, w, h,
                    gameScene.getGameEngine().getLevel().getPortals());

            Player p = gameScene.getGameEngine().getPlayer();

            p.setX(Double.parseDouble(pData[0])); p.setY(Double.parseDouble(pData[1]));
            p.getInventory().add(new ItemStack(1, Integer.parseInt(pData[2])));
            p.getInventory().add(new ItemStack(2, Integer.parseInt(pData[3])));
            if (pData.length > 4) {
                p.setHealth(Integer.parseInt(pData[4]));
            }
            gameScene.getGameEngine().clearEnemiesForCurrentLevel();
            int loadedEnemyCount = 0;
            boolean loadedArcherSection = false;
            String line = "";
            while (sc.hasNextLine()) { line = sc.nextLine().trim(); if (!line.isEmpty()) break; }
            if (!line.isEmpty()) {
                int nI = Integer.parseInt(line);
                for (int i = 0; i < nI; i++) {
                    String[] it = sc.nextLine().split(",");
                    lvl.dropItem(Double.parseDouble(it[0]), Double.parseDouble(it[1]), new ItemStack(Integer.parseInt(it[2]), 1));
                }
            }
            line = "";
            while (sc.hasNextLine()) { line = sc.nextLine().trim(); if (!line.isEmpty()) break; }
            if (!line.isEmpty()) {
                int nBts = Integer.parseInt(line);
                for (int i = 0; i < nBts; i++) {
                    String[] bt = sc.nextLine().split(",");
                    lvl.addBot(Double.parseDouble(bt[0]), Double.parseDouble(bt[1]));
                }
                loadedEnemyCount += nBts;
            }
            line = "";
            while (sc.hasNextLine()) { line = sc.nextLine().trim(); if (!line.isEmpty()) break; }
            if (!line.isEmpty()) {
                loadedArcherSection = true;
                int nArchers = Integer.parseInt(line);
                for (int i = 0; i < nArchers; i++) {
                    String[] archer = sc.nextLine().split(",");
                    lvl.addArcherBot(Double.parseDouble(archer[0]), Double.parseDouble(archer[1]));
                }
                loadedEnemyCount += nArchers;
            }
            if (loadedEnemyCount == 0) {
                gameScene.getGameEngine().spawnEnemiesForCurrentLevel();
            } else if (!loadedArcherSection) {
                gameScene.getGameEngine().spawnMissingArchersForCurrentLevel();
            }
            gameScene.show();
        } catch (Exception e) { e.printStackTrace(); } finally { sc.close(); }
    }
}
