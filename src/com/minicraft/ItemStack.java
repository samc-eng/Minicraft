package com.minicraft;

public class ItemStack {
    private final int itemId;
    private int amount;
    private int currentDurability;

    public ItemStack(int itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;

        // Si l'item a une durabilité, on l'initialise au max
        ItemDefinition def = ItemRegistry.get(itemId);
        this.currentDurability = (def != null && def.hasDurability) ? def.maxDurability : 0;
    }

 
    public boolean canStackWith(ItemStack other) {
        ItemDefinition def = ItemRegistry.get(this.itemId);
        if (def == null) return false;
        return this.itemId == other.itemId && def.maxStack > 1;
    }

    public boolean isFull() {
        ItemDefinition def = ItemRegistry.get(this.itemId);
        return def == null || this.amount >= def.maxStack;
    }

    
    public void add(int n)    { this.amount += n; }
    public void remove(int n) { this.amount -= n; }

    
    public void damage(int n) {
        this.currentDurability -= n;
    }

    public boolean isBroken() {
        ItemDefinition def = ItemRegistry.get(this.itemId);
        return def != null && def.hasDurability && this.currentDurability <= 0;
    }

 
    public int getItemId(){ return itemId; }
    public int getAmount(){ return amount; }
    public int getCurrentDurability() { return currentDurability; }
    public ItemDefinition getDefinition() {
        return ItemRegistry.get(itemId);
    }
}
