package com.minicraft;

/**
 * Gère l'armure équipée par le joueur (un seul emplacement).
 *
 * Au lieu d'absorber un nombre fixe de dégâts, l'armure donne une PROBABILITÉ
 * d'ignorer complètement un coup ("tanker") : sur un coup encaissé, le joueur
 * ne perd aucun cœur.
 *
 * IDs d'armure (voir ItemRegistry) :
 *   300 = Armure en fer  (tier 1) -> 30% de chance de tank
 *   310 = Armure en or   (tier 2) -> 60% de chance de tank
 *   320 = Armure en gem  (tier 3) -> 90% de chance de tank
 */
public class ArmorSlots {

    public static final int EMPTY = -1;

    // IDs reconnus comme armure, et leur probabilité de tank associée (même ordre).
    private static final int[]    ARMOR_IDS   = {300, 310, 320};
    private static final double[] TANK_CHANCE = {0.3, 0.6, 0.9};

    // ID de l'armure actuellement équipée (EMPTY si aucune).
    private int equipped = EMPTY;

    public ArmorSlots() {}

    /**
     * Équipe une armure si l'ID est valide.
     * @return l'ID de l'armure précédemment équipée (EMPTY si le slot était vide),
     *         ou EMPTY si l'item passé n'est pas une armure.
     */
    public int equip(int itemId) {
        if (!isArmor(itemId)) return EMPTY;
        int previous = this.equipped;
        this.equipped = itemId;
        return previous;
    }

    /** Retire l'armure équipée et retourne son ID (EMPTY si rien n'était équipé). */
    public int unequip() {
        int previous = this.equipped;
        this.equipped = EMPTY;
        return previous;
    }

    public int getEquipped()   { return this.equipped; }
    public boolean hasArmor()  { return this.equipped != EMPTY; }

    /**
     * Probabilité (0.0 à 1.0) d'ignorer un coup avec l'armure actuellement équipée.
     * Retourne 0.0 si aucune armure n'est équipée.
     */
    public double getTankChance() {
        for (int i = 0; i < ARMOR_IDS.length; i++) {
            if (equipped == ARMOR_IDS[i]) return TANK_CHANCE[i];
        }
        return 0.0;
    }

    /** True si l'item donné est une pièce d'armure reconnue. */
    public static boolean isArmor(int itemId) {
        for (int id : ARMOR_IDS) {
            if (id == itemId) return true;
        }
        return false;
    }
}
