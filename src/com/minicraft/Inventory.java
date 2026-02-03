package com.minicraft;

import java.util.HashMap;
import java.util.Map;

public class Inventory {
	private Map<Integer, Integer> inventaire = new HashMap<>();
	
	public void add(int id, int count) {
		int current = inventaire.getOrDefault(id,  0);
		inventaire.put(id, current+count);
	}
	
	public boolean has(int id, int count) {
		return inventaire.getOrDefault(id,0)>=count;
	}
	
	public boolean remove(int id, int count) {
		if (!has(id,count)) {return false;}
		int current = inventaire.getOrDefault(id,  0);
		inventaire.put(id, current-count);
		return true;
	}
	
	public int getAmount(int id) {
		return inventaire.getOrDefault(id, 0);
	}
	
}
