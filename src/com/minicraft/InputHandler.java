package com.minicraft;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.Scene;



public class InputHandler {
	private Set<KeyCode> activeKeys = new HashSet<>();
	private Set<KeyCode> previousKeys = new HashSet<>();
	
	//détecte le clavier:
	public InputHandler(Scene scene) {
		scene.setOnKeyPressed(e -> { activeKeys.add(e.getCode());});
		scene.setOnKeyReleased(e-> { activeKeys.remove(e.getCode());});		
	}
	
	//on met à jour quelle touche est enfoncée pour les cliquées
	public void update() {
		previousKeys.clear();
		previousKeys.addAll(activeKeys);
	}
	
	//regarde si la touche est maintenu
	public boolean isPressed(KeyCode key) {
		return activeKeys.contains(key);
	}
	
	//regarde si la touche est cliqué
	public boolean isClicked(KeyCode key) {
		return activeKeys.contains(key) && !previousKeys.contains(key);
	}
}
