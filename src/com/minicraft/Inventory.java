package com.minicraft;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
	private Map<Integer, Integer> inventaire = new HashMap<>();
	
	//ajout d'un item de type id
	public void add(int id, int count) {
		int current = inventaire.getOrDefault(id,  0);
		inventaire.put(id, current+count);
	}
	
	//demande si contient assez d'une même resource
	public boolean has(int id, int count) {
		return inventaire.getOrDefault(id,0)>=count;
	}
	
	//on retire une quantité si assez
	public boolean remove(int id, int count) {
		if (!has(id,count)) {return false;}
		int current = inventaire.getOrDefault(id,  0);
		inventaire.put(id, current-count);
		return true;
	}
	
	//on a quelle quantité :
	public int getAmount(int id) {
		return inventaire.getOrDefault(id, 0);
	}
	
}
