package com.minicraft;

import java.util.HashMap;
import java.util.Map;

public class Recipe {
	private String name;
	private int resultId;
	private int resultCount;
	private Map<Integer,Integer> costs = new HashMap<>();
	
	public Recipe(String name, int resultId, int resultCount) {
		this.name=name;
		this.resultId=resultId;
		this.resultCount=resultCount;
	}
	
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
	
	public void Craft(Inventory inventaire) {
		if (canCraft(inventaire)) {
			for (Integer id : costs.keySet()) {
				inventaire.remove(id, costs.get(id));
			}
			
			inventaire.add(resultId, resultCount);
			System.out.println("Craft réussi : " + this.name);
		}
	}
	
	public String getName() { return name;}
}
