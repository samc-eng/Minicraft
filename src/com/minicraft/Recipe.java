package com.minicraft;

import java.util.HashMap;
import java.util.Map;

public class Recipe {
    private String name;
    private int resultId;     // l'id de ce qu'on obtient
    private int resultCount;  // combien on en obtient
    private Map<Integer,Integer> costs = new HashMap<>(); // les ingredients : id -> nombre

    public Recipe(String name, int resultId, int resultCount) {
        this.name=name;
        this.resultId=resultId;
        this.resultCount=resultCount;
    }

    // ajoute un ingredient a la recette
    public Recipe addCost(int id, int count) {
        costs.put(id, count);
        return this;
    }

    // est-ce que le joueur a tout ce qu'il faut ?
    public boolean canCraft(Player player) {
        for (Integer id : costs.keySet()) {
            if (!player.has(id, costs.get(id))) {
                return false;
            }
        }
        return true;
    }

    // fait le craft si on peut : on retire les ingredients et on donne le resultat
    public void Craft(Player player) {
        if (canCraft(player)) {
            for (Integer id : costs.keySet()) {
                player.consume(id, costs.get(id));
            }
            player.pickUpItem(new ItemStack(resultId, resultCount));
            System.out.println("Craft réussi : " + this.name);
        }
    }

    public String getName()      { return name; }
    public int getResultId()     { return resultId; }
    public int getResultCount()  { return resultCount; }
    public Map<Integer,Integer> getCosts() { return costs; }
}
