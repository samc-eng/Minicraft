package com.minicraft;

import javafx.scene.image.Image;

public class ItemDefinition {
	public final int id;
    public final String name;
    public final int maxStack;        // 64 pour ressources, 1 pour outils
    public final boolean placeable;
    public final boolean hasDurability;
    public final int maxDurability;
    public final Image texture;

    public ItemDefinition(int id, String name, int maxStack, boolean placeable, boolean hasDurability, int maxDurability, String imagePath) {
        this.id = id;
        this.name = name;
        this.maxStack = maxStack;
        this.placeable = placeable;
        this.hasDurability = hasDurability;
        this.maxDurability = maxDurability;
        this.texture=TextureManager.getTexture(imagePath);
    }

    public Image getTexture(){ return this.texture; }
}
