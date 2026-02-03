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
	private int widthScreen=400;
	private int heightScreen=400;
	private InventoryUI inventaireUI;
	private boolean inventaireOuvert=false;
	private CraftingUI craftingUI;
	private boolean craftingOpen=false;

	static public void main(String[] args) {
		launch(args);
	}
	
	
	@Override
	public void start(Stage primaryStage) {
		Canvas canva = new Canvas(400,400);
		this.pinceau = canva.getGraphicsContext2D();
		this.player = new Player(50,50);
		this.level = new Level(4000,4000);
		
		Group root = new Group();
		root.getChildren().add(canva);
		
		Scene scene = new Scene(root,400,400);
		input = new InputHandler(scene);
		primaryStage.setTitle("Minicraft clone");
		primaryStage.setScene(scene);
		
		craftingUI= new CraftingUI();
		craftingUI.addRecipe(new Recipe("Mur de pierre", 3, 1).addCost(1, 5));
		
		inventaireUI= new InventoryUI();
		
		AnimationTimer timer = new AnimationTimer() {
			public void handle(long now) {
				double camX=player.getX()-widthScreen/2;
				double camY=player.getY()-heightScreen/2;
				
				if (camX < 0) {camX = 0;}
				if (camY < 0) {camY = 0;}
				if (camX > (level.getWidth() * 16) - 400) {camX = (level.getWidth() * 16) - 400;}
				if (camY > (level.getHeight() * 16) - 400) {camY = (level.getHeight() * 16) - 400;}
				
				if (input.isClicked(KeyCode.E)) {
					inventaireOuvert = !inventaireOuvert;
					craftingOpen=false;
				}
				
				if (input.isClicked(KeyCode.C)) {
					craftingOpen = !craftingOpen;
					inventaireOuvert=false;
				}
				
				pinceau.clearRect(0, 0, 400, 400);
				pinceau.save();
				pinceau.translate(-camX, -camY);
				
				level.render(pinceau, camX, camY);
				
				if (inventaireOuvert) {
					
				} else if (craftingOpen) {
					craftingUI.tick(input, player.getInventory());
				} else {
					player.tick(level, input);
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
