package com.minicraft;

import javafx.scene.image.Image;
import java.util.HashMap;

public class TextureManager {
    private final static HashMap<String,Image> textures= new HashMap<>();


    public static Image getTexture(String path){
        if (textures.containsKey(path)){
            return textures.get(path);
        }

        Image img = new Image("file:resources/textures/" + path);
        if (img.isError()) {
            System.err.println("[TextureManager] ERREUR chargement : file:resources/textures/" + path);
        }
        textures.put(path, img);
        return textures.get(path);
        
    }
}
