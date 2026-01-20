package com.minicraft;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.Scene;



public class InputHandler {
	private Set<KeyCode> activeKeys = new HashSet<>();
	private Set<KeyCode> previousKeys = new HashSet<>();
	
	public InputHandler(Scene scene) {
		scene.setOnKeyPressed(e -> { activeKeys.add(e.getCode());});
		scene.setOnKeyReleased(e-> { activeKeys.remove(e.getCode());});		
	}
	
	public void update() {
		previousKeys.clear();
		previousKeys.addAll(activeKeys);
	}
	
	public boolean isPressed(KeyCode key) {
		return activeKeys.contains(key);
	}
	
	public boolean isClicked(KeyCode key) {
		return activeKeys.contains(key) && !previousKeys.contains(key);
	}
}
