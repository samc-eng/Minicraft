package com.minicraft;

public class PlaceableItem extends Item {
    private final int blockId;

    public PlaceableItem(double x, double y, int itemId, int blockId) {
        super(x, y, itemId);
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
