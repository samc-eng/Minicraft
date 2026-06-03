package com.minicraft;

import java.util.HashMap;
import java.util.Map;

public class Recipe {
    private String name;
    private int resultId; //quel id/type ?
    private int resultCount; //la quantite obtenu
    private Map<Integer,Integer> costs = new HashMap<>();
    
    public Recipe(String name, int resultId, int resultCount) {
        this.name=name;
        this.resultId=resultId;
        this.resultCount=resultCount;
    }
    
    // cout d'un craft :
    public Recipe addCost(int id, int count) {
        costs.put(id, count);
        return this;
    }
    
    public boolean canCraft(Inventory inventaire) {
        for (Integer id : costs.keySet()) {
            if (!inventaire.has(id, costs.get(id))) {
                return false;
            }
        }
        return true;
    }
    
    public void Craft(Player player) {
        Inventory inventaire = player.getInventory();
        
        if (canCraft(inventaire)) {
            // 1. On consomme les ressources dans l'inventaire caché
            for (Integer id : costs.keySet()) {
                inventaire.remove(id, costs.get(id));
            }
            
            // L'objet créé passe par le Player, ce qui va l'ajouter à la fois à l'inventaire ET à la Hotbar !
            player.pickUpItem(new ItemStack(resultId, resultCount));
            
            System.out.println("Craft réussi : " + this.name);
        }
    }
    
    public String getName() { return name;}
}
