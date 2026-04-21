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
 * Elle fait le pont entre le Menu Principal et le moteur de jeu de Samuel.
 */
public class GameScene {
    // --- VARIABLES DE CLASSE (Attributs) ---
    private MainMenu mainMenu;   // Référence vers le menu pour pouvoir y retourner
    private Stage stage;        // La fenêtre principale de l'application
    private Scene scene;        // Le conteneur qui contient tous nos éléments graphiques
    private Pane gameView;      // Le panneau (Canvas) où le jeu est dessiné
    private Main gameEngine;    // L'instance du moteur de jeu (Main de Samuel)
    private StackPane root;     // Le conteneur de type "Pile" (pour empiler le bouton sur le jeu)

    /**
     * CONSTRUCTEUR : Prépare tout le nécessaire pour l'écran de jeu.
     */
    public GameScene(Stage stage, MainMenu mainMenu) {
        this.stage = stage;
        this.mainMenu = mainMenu;

        // Le StackPane permet de superposer des éléments (comme des calques Photoshop)
        this.root = new StackPane();

        // --- ÉTAPE 1 : CRÉATION DE LA SCÈNE ---
        // On définit la taille de la fenêtre (1200x800) et on lui donne le 'root' comme base
        this.scene = new Scene(this.root, 1200, 800);

        // --- ÉTAPE 2 : INITIALISATION DU JEU ---
        this.gameEngine = new Main(); // Création du moteur
        // On demande au moteur de nous donner sa vue en lui "prêtant" la scène
        // Cela permet à l'InputHandler de Samuel de fonctionner sur NOTRE scène
        this.gameView = gameEngine.getGameView(stage, this.scene);

        // --- ÉTAPE 3 : CRÉATION DU BOUTON DE RETOUR ---
        Button btnBack = new Button("Menu principale (L)");
        styleButton(btnBack); // Application du design CSS

        // Définit ce qui se passe quand on clique sur le bouton à la souris
        btnBack.setOnAction(e -> returnToMenu());

        // IMPORTANT : Empêche le bouton de "voler" le focus.
        // Sans ça, après un clic, les touches ZQSD ne fonctionneraient plus.
        btnBack.setFocusTraversable(false);

        // --- ÉTAPE 4 : ASSEMBLAGE DES COUCHES ---

        // On ajoute d'abord le jeu (il sera au fond)
        if (this.gameView != null) {
            this.root.getChildren().add(this.gameView);
        }

        // Configuration du placement du bouton par-dessus le jeu
        // On l'aligne en haut à droite
        StackPane.setAlignment(btnBack, Pos.TOP_RIGHT);
        // On lui met une marge de 15 pixels pour ne pas qu'il colle aux bords
        StackPane.setMargin(btnBack, new Insets(15));

        // On ajoute le bouton en dernier pour qu'il soit au premier plan
        this.root.getChildren().add(btnBack);
    }

    /**
     * Méthode pour quitter le jeu et revenir au menu principal.
     */
    private void returnToMenu() {
        System.out.println(">>> ACTION : Sauvegarde et retour au menu <<<");
        // On demande au menu de se réafficher dans la fenêtre actuelle
        mainMenu.show(stage);
    }

    /**
     * Gère l'apparence visuelle du bouton (Couleurs, bords arrondis, effets de survol).
     */
    private void styleButton(Button btn) {
        // Style de base (bleu sombre)
        String styleNormal = "-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;";
        // Style quand la souris passe dessus (plus clair)
        String styleHover = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;";

        btn.setStyle(styleNormal);

        // Changement de style dynamique au survol
        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e -> btn.setStyle(styleNormal));
    }

    /**
     * Affiche réellement la scène à l'écran et active les écouteurs de touches.
     */
    public void show() {
        // On applique notre scène à la fenêtre principale
        stage.setScene(this.scene);

        // --- GESTION DU CLAVIER (TOUCHE L) ---
        // On ajoute un filtre sur la fenêtre pour intercepter la touche L avant tout le monde
        stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.L) {
                System.out.println(">>> TOUCHE L DÉTECTÉE PAR LE STAGE <<<");
                returnToMenu();
                event.consume(); // On "consomme" l'événement pour qu'il ne s'envoie pas au jeu
            }
        });

        // On force le focus sur la zone de jeu pour que ZQSD marche tout de suite
        root.requestFocus();
        if (gameView != null) {
            gameView.requestFocus();
        }
    }
}