package com.minicraft;

public class ResourceItem extends Item {
	 
	public ResourceItem(double x, double y, ItemStack stack) {
		super(x,y,stack);
	}
	
	@Override
	public boolean isPlaceable() {
		return false;
	}

}
