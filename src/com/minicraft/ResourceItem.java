package com.minicraft;

public class ResourceItem extends Item {
	 
	public ResourceItem(double x, double y, int itemID) {
		super(x,y,itemID);
	}
	
	@Override
	public boolean isPlaceable() {
		return false;
	}

}
