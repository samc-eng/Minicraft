package com.minicraft;

/**
 * ArmorSlots gere les 4 emplacements d'armure du joueur :
 *   - casque (slot 0)
    *   - plastron (slot 1)
    *   - jambieres (slot 2)
    *   - bottes (slot 3)
    *
    * Reduction de degats par materiau (par piece) :
   *   Cuir : 1 | Fer : 2 | Or : 1.5 | Gem : 3
   * Reduction totale max (4 pieces fer) : 8 points absorbes sur chaque coup.
   */
public class ArmorSlots {

    // IDs des armures equipees (-1 = vide)
    private int[] slots = {-1, -1, -1, -1};

    // Noms des slots pour affichage
    public static final String[] SLOT_NAMES = {"Casque", "Plastron", "Jambieres", "Bottes"};

    // Reduction de degats (points) par piece selon le materiau
    // Cuir=300-303, Fer=310-313, Or=320-323, Gem=330-333
    private static final int[] ARMOR_BASES = {300, 310, 320, 330};
      private static final double[] ARMOR_REDUCTION = {1.0, 2.0, 1.5, 3.0};

    public ArmorSlots() {}

    /**
     * Equipe un item dans le bon slot selon son ID.
       * Retourne true si l'item est bien une armure, false sinon.
       */
    public boolean equip(int itemId) {
              int slot = getSlotForItem(itemId);
              if (slot == -1) return false;
              slots[slot] = itemId;
              return true;
    }

    /**
     * Retire l'armure d'un slot (0-3).
       */
    public void unequip(int slot) {
              if (slot >= 0 && slot < 4) slots[slot] = -1;
    }

    /**
     * Retourne l'ID de l'armure dans le slot donne, ou -1 si vide.
       */
    public int getSlot(int slot) {
              return slots[slot];
    }

    /**
     * Calcule la reduction totale de degats grace a l'armure equipee.
       * Exemple : plastron en fer = 2 points absorbes.
       */
    public int getArmorReduction() {
              double total = 0;
              for (int slot = 0; slot < 4; slot++) {
                            int id = slots[slot];
                            if (id == -1) continue;
                            for (int m = 0; m < ARMOR_BASES.length; m++) {
                                              int base = ARMOR_BASES[m];
                                              // IDs du materiau m : base, base+1, base+2, base+3
                                if (id >= base && id <= base + 3) {
                                                      total += ARMOR_REDUCTION[m];
                                                      break;
                                }
                            }
                          }
              return (int) total;
    }

    /**
     * Retourne le slot (0-3) correspondant a un itemId d'armure.
       * -1 si ce n'est pas une armure.
       * piece 0=casque, 1=plastron, 2=jambieres, 3=bottes
       */
    public static int getSlotForItem(int itemId) {
              for (int base : ARMOR_BASES) {
                            if (itemId >= base && itemId <= base + 3) {
                                              return itemId - base; // 0=casque, 1=plastron, 2=jambieres, 3=bottes
                            }
              }
              return -1;
    }

    /**
     * Retourne true si l'item est une armure.
       */
    public static boolean isArmor(int itemId) {
              return getSlotForItem(itemId) != -1;
    }
}
