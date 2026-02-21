package com.minicraft;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Main extends Application{
	private Player player;
	private GraphicsContext pinceau;
	private Level level;
	private InputHandler input;
	private double widthScreen;
	private double heightScreen;
	private InventoryUI inventaireUI;
	private boolean inventaireOuvert=false;
	private CraftingUI craftingUI;
	private boolean craftingOpen=false;

	static public void main(String[] args) {
		launch(args);
	}
	
	
	@Override
	public void start(Stage primaryStage) {
		Canvas canva = new Canvas(Config.SCREEN_WIDTH,Config.SCREEN_HEIGHT);
		this.pinceau = canva.getGraphicsContext2D();
		this.player = new Player(50,50);
		this.level = new Level(4000,4000);
		
		//on crée une racine et un canva qui s'adapte à la taille de la fenêtre
		javafx.scene.layout.Pane root = new javafx.scene.layout.Pane();
		root.getChildren().add(canva);
		
		canva.widthProperty().bind(root.widthProperty());
		canva.heightProperty().bind(root.heightProperty());
		
		Scene scene = new Scene(root,Config.SCREEN_WIDTH,Config.SCREEN_HEIGHT);
		primaryStage.setTitle("Minicraft");
		primaryStage.setScene(scene);
		
		//on connecte l'entrée du clavier
		input = new InputHandler(scene);
		craftingUI= new CraftingUI();
		//on crée la recette du mur de pierre
		craftingUI.addRecipe(new Recipe("Mur de pierre", 3, 1).addCost(1, 5));
		
		inventaireUI= new InventoryUI();
		
		//boucle de jeu :
		AnimationTimer timer = new AnimationTimer() {
			public void handle(long now) {
				//on recupere la taille de la fenetre:
				widthScreen=canva.getWidth();
				heightScreen=canva.getHeight();
				
				//on transforme le rendu de la caméra pourqu'elle corresponde au zoom
				double largeurVue = widthScreen/Config.SCALE;
				double hauteurVue = heightScreen/Config.SCALE;

				
				double camX=player.getX()+Config.blockSize/2-largeurVue/2;
				double camY=player.getY()+Config.blockSize/2-hauteurVue/2;
				
				//on fixe la caméra sur les côtés
				if (camX < 0) {camX = 0;}
				if (camY < 0) {camY = 0;}
				if (camX > (level.getWidth() * Config.blockSize) - largeurVue) {camX = (level.getWidth() * Config.blockSize) - largeurVue;}
				if (camY > (level.getHeight() * Config.blockSize) - hauteurVue) {camY = (level.getHeight() * Config.blockSize) - hauteurVue;}
				
				if (input.isClicked(KeyCode.E)) {
					inventaireOuvert = !inventaireOuvert;
					craftingOpen=false;
				}
				
				if (input.isClicked(KeyCode.C)) {
					craftingOpen = !craftingOpen;
					inventaireOuvert=false;
				}
				
				pinceau.clearRect(0, 0, widthScreen, heightScreen); //a chaque passage on efface tout
				pinceau.save();
				pinceau.scale(Config.SCALE, Config.SCALE); //on effectue le zoom
				pinceau.translate(-camX, -camY); //on translate l'image selon la position de la caméra
				
				level.render(pinceau, camX, camY, largeurVue, hauteurVue);//rendue du monde visible dessiné
				
				if (inventaireOuvert) {
					
				} else if (craftingOpen) {
					craftingUI.tick(input, player.getInventory()); //maj du craft sélectionné
				} else {
					player.tick(level, input); //interaction du joueur
					level.updateItems(player);
				}
				
				player.render(pinceau);		
				pinceau.restore();
				
				if (craftingOpen) {
					craftingUI.render(pinceau, player.getInventory());
				} else if (inventaireOuvert) {
					inventaireUI.render(pinceau, player);					
				} else {
					pinceau.setFill(Color.color(0, 0, 0, 0.5));
				    pinceau.fillRect(10, 10, 100, 30);
				    pinceau.setFill(Color.WHITE);
				    pinceau.fillText("Roche : " + player.getInventory().getAmount(1), 20, 30);
				}					
				input.update();
			}
		};
		
		timer.start();
		primaryStage.show();		
	}
}
