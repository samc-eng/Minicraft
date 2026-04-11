package com.minicraft;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
	private List<ItemStack> inventaire = new ArrayList<>();
	
	//ajout d'un item de type id
	public void add(int itemId, int count) {
        if (!ItemRegistry.exists(itemId)) return;

        ItemDefinition def = ItemRegistry.get(itemId);

        // Si stackable, on cherche une pile existante non pleine
        if (def.maxStack > 1) {
            for (ItemStack stack : inventaire) {
                if (stack.getItemId() == itemId && !stack.isFull()) {
                    stack.add(count);
                    return;
                }
            }
        }

        // Sinon (outil ou pas de place) → nouvelle pile
        inventaire.add(new ItemStack(itemId, count));
    }
	
	//demande si contient assez d'une même resource
	public boolean has(int id, int count) {
		return this.getAmount(id) >= count;
	}
	
	//on retire une quantité si assez
	public boolean remove(int itemId, int count) {
		if (!has(itemId, count)) { return false; }
        
        int restantARetirer = count;
        // On parcourt l'inventaire pour vider les piles
        for (int i = inventaire.size() - 1; i >= 0; i--) {
            ItemStack stack = inventaire.get(i);
            if (stack.getItemId() == itemId) {
                if (stack.getAmount() > restantARetirer) {
                    stack.remove(restantARetirer);
                    return true;
                } else {
                    restantARetirer -= stack.getAmount();
                    inventaire.remove(i); // La pile est vide, on retire la case !
                    if (restantARetirer == 0) {
                    	return true;
                    }
                }
            }
        }
        return true;
	}
	
	//on a quelle quantité :
	public int getAmount(int id) {
	    int total = 0;
	    for (ItemStack stack : inventaire) {
	        if (stack.getItemId() == id) {
	        	total += stack.getAmount();
	        }
	    }
	    return total;
	}
	
	// Pour les outils : retourne toutes les instances d'un item
    public List<ItemStack> getAll(int itemId) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : inventaire) {
            if (stack.getItemId() == itemId) {
            	result.add(stack);
            }
        }
        return result;
    }

    public List<ItemStack> getAll() { return inventaire; }
	
}
