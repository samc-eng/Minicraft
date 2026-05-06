package com.minicraft;

import javafx.scene.image.Image;
import java.util.HashMap;

public class TextureManager {
    private final static HashMap<String,Image> textures= new HashMap<>();


    public static Image getTexture(String path){
        if (textures.containsKey(path)){
            return textures.get(path);
        }

        textures.put(path, new Image("file:resources/textures/" + path));
        return textures.get(path);
        
    }
}
