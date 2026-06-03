package com.minicraft;

public class PlaceableItem extends Item {
    private final int blockId;

    public PlaceableItem(double x, double y, ItemStack stack, int blockId) {
        super(x, y, stack);
        this.blockId = blockId;
    }

    @Override
    public boolean isPlaceable() {
        return true;
    }

    public int getBlockId() {
        return blockId;
    }
}
