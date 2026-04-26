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

/**
 * Interface graphique d'accueil du jeu.
 * Permet au joueur de lancer une nouvelle partie ou d'en charger une.
 * @author Ali KIES
 */

public class MainMenu {
    private MediaPlayer mediaPlayer; // déclaration du mediaPlayer pour lancer la musique
    int largeurBouton = 200;
    // -- Définition du style CSS (Arrondi, Gras, Couleurs vives) -- //
    String styleBouton =
            "-fx-background-color: linear-gradient(#5dade2, #2e86c1); " + // Dégradé bleu
                    "-fx-background-radius: 30; " +                             // Bords très arrondis
                    "-fx-background-insets: 0; " +
                    "-fx-text-fill: white; " +                                  // Texte blanc
                    "-fx-font-family: 'Arial Rounded MT Bold'; " +              // Police arrondie
                    "-fx-font-weight: bold; " +                                 // TEXTE EN GRAS
                    "-fx-font-size: 18px; " +
                    "-fx-padding: 10 20 10 20; " +                              // Espace interne
                    "-fx-cursor: hand; " +                                      // Curseur "main"
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 4);"; // Ombre

    // TODO : Gestion des sauvegardes de partie
    //private SaveManager saveManager; //déclaration de la variable que l'on va utiliser pour gérer les sauvergardes de parties

    public MainMenu() {
        /*this.saveManager = new SaveManager(); */ //initialisation du gestionnaire de sauvegarde que je vais créer
    }

    /**
     * Construit et affiche le menu principal sur la fenêtre du jeu.
     * @param window La fenêtre principale (Stage) de JavaFX.
     */

    public void show(Stage window) {

// -- I. Mise en place de la musique de fond -- //
        try {
            Media hit = new Media(new java.io.File("resources/menu_music.mp3").toURI().toString());
            this.mediaPlayer = new MediaPlayer(hit);
            this.mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            this.mediaPlayer.setVolume(0.5);
            this.mediaPlayer.play();
        } catch (Exception e) {
            System.out.println("Erreur audio : " + e.getMessage());
        }

// -- II. CRÉATION DU LAYOUT -- //
        StackPane root = new StackPane(); // superposition des éléments (fond + menu)

        // --- II.1 Chargement de l'image de fond, avec test suite au différent problème du départ -- //
        try {
            java.io.File fichier = new java.io.File("resources/bg.png");
            if (!fichier.exists()) {
                System.out.println("❌ Fichier bg.png introuvable !");
                root.setStyle("-fx-background-color: red;");
            } else {
                javafx.scene.image.Image backgroundImage = new javafx.scene.image.Image(fichier.toURI().toString());
                javafx.scene.image.ImageView backgroundView = new javafx.scene.image.ImageView(backgroundImage);
                backgroundView.fitWidthProperty().bind(window.widthProperty());
                backgroundView.fitHeightProperty().bind(window.heightProperty());
                backgroundView.setPreserveRatio(false);
                root.getChildren().add(0, backgroundView);
                System.out.println("✅ Image chargée !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // -- II.2 Ajout du nom du jeu AVEC EFFETS ET ANIMATION -- //
        Text title = new Text("MINICRAFT");

        // style du texte //
        title.setFont(Font.font("VERDANA", FontWeight.BOLD, 100)); // Très grand
        title.setFill(Color.GOLD); // Intérieur blanc
        title.setStroke(Color.BROWN); // Contour noir épais
        title.setStrokeWidth(3);

        // configuration de l'animation du texte //
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2), title);

        // définition de la taille de départ //
        pulse.setFromX(0.8); // 80% de la taille
        pulse.setFromY(0.8);

        //définition l'agrandissement maximal //
        pulse.setToX(1.2); // Zoom sur l'axe X (largeur)
        pulse.setToY(1.2); // Zoom sur l'axe Y (hauteur)

        // Mouvement fluide ("Ease-both" pour ralentir au début et à la fin) //
        pulse.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

        // Activation de l'effet "Yoyo" (s'agrandit PUIS rétrécit au lieu de recommencer à zéro) //
        pulse.setAutoReverse(true);

        // Répétition à l'infini de l'animation //
        pulse.setCycleCount(Animation.INDEFINITE);

        // Lancement de l'animation //
        pulse.play();



        // -- II.3 Création des bouton et d style de ma page -- //

        VBox menuBox = new VBox(60); // VBox(40) : On crée une colonne verticale avec 20 pixels d'espace entre chaque bouton
        menuBox.setAlignment(Pos.CENTER); // Aligne tout le contenu de la colonne exactement au milieu de l'écran
        menuBox.setStyle("-fx-background-color: transparent; -fx-padding: 50;");

        // Création du Bouton 1 permettant de débuter une nouvelle partie
        Button btnNewGame = new Button("Créer un nouveau monde");
        btnNewGame.setMinWidth(largeurBouton);
        btnNewGame.setStyle(null); // On repasse en style par défaut (rectangle gris)
        btnNewGame.setOnAction(e -> startNewGame(window));
        btnNewGame.setOnMouseEntered(e-> btnNewGame.setStyle(styleBouton + "-fx-background-color: #3498db; -fx-scale-x: 1.1; -fx-scale-y: 1.1;"));
        btnNewGame.setOnMouseExited(e -> btnNewGame.setStyle(null));

        // Création du Bouton 2 pour continuer
        Button btnLoadGame = new Button("Continuer la partie");
        btnLoadGame.setMinWidth(largeurBouton);
        btnLoadGame.setStyle(null);
        btnLoadGame.setOnAction(e -> loadExistingGame(window));
        btnLoadGame.setOnMouseEntered(e-> btnLoadGame.setStyle(styleBouton + "-fx-background-color: #3498db; -fx-scale-x: 1.1; -fx-scale-y: 1.1;"));
        btnLoadGame.setOnMouseExited(e -> btnLoadGame.setStyle(null));

        // Création du Bouton 3: quitter
        Button btnQuitter= new Button ("Quitter");
        btnQuitter.setMinWidth(largeurBouton);
        btnQuitter.setStyle(null);
        btnQuitter.setOnMouseEntered(e-> btnQuitter.setStyle(styleBouton + "-fx-background-color: #3498db; -fx-scale-x: 1.1; -fx-scale-y: 1.1;"));
        btnQuitter.setOnMouseExited(e -> btnQuitter.setStyle(null));
        btnQuitter.setOnAction(e -> window.close());

        // Création du Bouton 4: Information
        Button btnInfos= new Button ("Informations");
        btnInfos.setMinWidth(largeurBouton);
        btnInfos.setStyle(null);
        btnInfos.setOnMouseEntered(e-> btnInfos.setStyle(styleBouton + "-fx-background-color: #3498db; -fx-scale-x: 1.1; -fx-scale-y: 1.1;"));
        btnInfos.setOnMouseExited(e -> btnInfos.setStyle(null));

// -- III : Assemblage --//

        // On ajoute les 4 boutons dans la colonne (VBox) //
        menuBox.getChildren().addAll(title, btnNewGame, btnLoadGame, btnQuitter, btnInfos);
        root.getChildren().add(menuBox);
        Scene menuScene = new Scene(root, 1000, 700);

        // On place la scène dans la fenêtre (Stage) //
        window.setScene(menuScene);
        window.setTitle("Minicraft - Menu Principal");

        // Affichage de la fenêtre //
        root.requestFocus();
        window.show();
    }

    /**
     * Callback : Action déclenchée par le bouton "créer un nouveau Monde".
     */

    private void startNewGame(Stage window) {
        System.out.println("Création d'un nouveau monde !");

        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        GameScene gameScene = new GameScene(window, this);
        gameScene.show();
    }

    /**
     * Callback : Action déclenchée par le bouton "Continuer".
     */

    private void loadExistingGame(Stage window) {
        Scanner sc = SaveManager.getSaveScanner();

        if (sc != null && sc.hasNextLine()) {
            if (mediaPlayer != null) mediaPlayer.stop();

            // 1. Lire les données du joueur
            String[] pData = sc.nextLine().split(",");
            double px = Double.parseDouble(pData[0]);
            double py = Double.parseDouble(pData[1]);
            int roche = Integer.parseInt(pData[2]);
            int bois = Integer.parseInt(pData[3]);

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

            Player p = gameScene.getGameEngine().getPlayer();
            p.setX(px);
            p.setY(py);

            // --- REDONNER L'INVENTAIRE AU JOUEUR ---
            // On utilise la méthode add de Samuel pour injecter les ressources chargées
            p.getInventory().add(1, roche); // ID 1 pour la roche
            p.getInventory().add(2, bois);  // ID 2 pour le bois

            gameScene.show();
            sc.close();
        }
    }
}