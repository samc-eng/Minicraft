package com.minicraft;

public class Drop {
    public final int itemId;
    public final int minAmount;
    public final int maxAmount;
    public final double chance; // 1.0 = 100%, 0.2 = 20%

    public Drop(int itemId, int minAmount, int maxAmount, double chance) {
        this.itemId = itemId;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.chance = chance;
    }

    public int rollAmount() {
        if (minAmount == maxAmount) return minAmount;
        return minAmount + (int)(Math.random() * (maxAmount - minAmount + 1));
    }
 
    public boolean rolls() {
        return Math.random() <= chance;
    }

}
