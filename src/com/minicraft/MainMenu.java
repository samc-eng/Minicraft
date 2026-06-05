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
        // on lance la musique une seule fois pour pas qu'elle se double quand on revient au menu
        if (this.mediaPlayer == null) {
            try {
                Media hit = new Media(new java.io.File("resources/menu_music.mp3").toURI().toString());
                this.mediaPlayer = new MediaPlayer(hit);
                this.mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                this.mediaPlayer.setVolume(0.5);
                this.mediaPlayer.play();
            } catch (Exception e) { 
                
            }
        }

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

        Button btnNewGame = createStyledButton("Créer un nouveau monde");
        btnNewGame.setOnAction(e -> startNewGame(window));

        Button btnLoadGame = createStyledButton("Continuer la partie");
        btnLoadGame.setOnAction(e -> loadExistingGame(window));

        Button btnQuitter = createStyledButton("Quitter");
        btnQuitter.setOnAction(e -> window.close());

        menuBox.getChildren().addAll(title, btnNewGame, btnLoadGame, btnQuitter);
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

        // On remet les réglages : neutre au départ, style au survol
        btn.setStyle(null);
        btn.setOnMouseEntered(e -> btn.setStyle(styleBouton + "-fx-scale-x: 1.1; -fx-scale-y: 1.1;"));
        btn.setOnMouseExited(e -> btn.setStyle(null));

        return btn;
    }

    private void startNewGame(Stage window) {
        GameScene gameScene = new GameScene(window, this);
        gameScene.show();
    }

    private void loadExistingGame(Stage window) {
        Scanner sc = SaveManager.getSaveScanner();
        if (sc == null) return;
        try {
            // position du joueur
            String[] pData = sc.nextLine().split(",");

            // on cree la scene (le moteur genere une surface temporaire, on la remplacera)
            GameScene gameScene = new GameScene(window, this);

            // profondeur et coordonnees du portail d'entree de la grotte
            String[] stateData = sc.nextLine().split(",");
            int savedDepth = Integer.parseInt(stateData[0]);
            int lpx = Integer.parseInt(stateData[1]);
            int lpy = Integer.parseInt(stateData[2]);
            int[] lastPortal = (lpx >= 0 && lpy >= 0) ? new int[]{lpx, lpy} : null;

            // on lit la surface
            Level surface = readLevel(sc);

            // et la grotte si elle existe
            Level cave = null;
            String cavePresent = sc.nextLine().trim();
            if (cavePresent.equals("1")) {
                cave = readLevel(sc);
            }

            // on remplace l'etat du moteur par ce qu'on a charge
            gameScene.getGameEngine().loadGameState(surface, cave, savedDepth, lastPortal);

            Player p = gameScene.getGameEngine().getPlayer();
            p.setX(Double.parseDouble(pData[0]));
            p.setY(Double.parseDouble(pData[1]));

            if (pData.length > 4) {
                p.setHealth(Integer.parseInt(pData[4]));
            }
            gameScene.getGameEngine().clearEnemiesForCurrentLevel();
            int loadedEnemyCount = 0;

            // 7. Inventaire complet (Ton code)
            String line = "";
            while (sc.hasNextLine()) { line = sc.nextLine().trim(); if (!line.isEmpty()) break; }
            if (!line.isEmpty()) {
                int nInv = Integer.parseInt(line);
                for (int i = 0; i < nInv; i++) {
                    String[] st = sc.nextLine().split(",");
                    int id  = Integer.parseInt(st[0]);
                    int qty = Integer.parseInt(st[1]);
                    int dur = Integer.parseInt(st[2]);
                    p.getInventory().add(new ItemStack(id, qty, dur));
                }
            }
            

            // et la hotbar
            line = "";
            while (sc.hasNextLine()) { line = sc.nextLine().trim(); if (!line.isEmpty()) break; }
            if (!line.isEmpty()) {
                String[] slots = line.split(";", -1);
                ItemStack[] hb = p.getHotbar();
                for (int i = 0; i < slots.length && i < hb.length; i++) {
                    String s = slots[i];
                    if (s.equals("-") || s.isEmpty()) {
                        hb[i] = null;
                    } else {
                        String[] parts = s.split(",");
                        hb[i] = new ItemStack(Integer.parseInt(parts[0]),
                                              Integer.parseInt(parts[1]),
                                              Integer.parseInt(parts[2]));
                    }
                }
            }


            // 9. Archers (Ajout de Yazid)
            // On détermine dans quel niveau le joueur se trouve pour y placer les archers
            Level currentLevel = (savedDepth == 1 && cave != null) ? cave : surface;
            
            line = "";
            while (sc.hasNextLine()) { line = sc.nextLine().trim(); if (!line.isEmpty()) break; }
            if (!line.isEmpty()) {
                int nArchers = Integer.parseInt(line);
                for (int i = 0; i < nArchers; i++) {
                    String[] archer = sc.nextLine().split(",");
                    if (currentLevel != null) {
                        currentLevel.addArcherBot(Double.parseDouble(archer[0]), Double.parseDouble(archer[1]));
                    }
                }
                loadedEnemyCount += nArchers;
            }

            //10. Gestion du spwan de secours
            if (loadedEnemyCount == 0) {
                gameScene.getGameEngine().spawnEnemiesForCurrentLevel();
            }

            gameScene.show();
        } catch (Exception e) { e.printStackTrace(); } finally { sc.close(); }
    }

    // on reconstruit un niveau a partir du fichier de save (delegue a SaveManager)
    private Level readLevel(Scanner sc) {
        return SaveManager.readLevel(sc);
    }
}
