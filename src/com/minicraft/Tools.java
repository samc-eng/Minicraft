package com.minicraft;

/**
 * Utilitaire centralisant TOUTE la logique des outils du jeu.
 *
 * Convention des IDs (voir ItemRegistry, plage 200-245) :
 *   Base matériau : 200=bois, 210=pierre, 220=fer, 230=or, 240=gemme
 *   Décalage type : +0=épée, +1=hache, +2=pioche, +3=pelle, +4=houe, +5=arc
 *   Exemple : 222 = 220 (fer) + 2 (pioche) = Pioche en fer.
 *
 * Idée clé : plus le tier de l'outil est élevé, moins l'action coûte d'énergie.
 */
public class Tools {

    // --- Types d'outils (décalage de l'ID dans la dizaine) ---
    public static final int TYPE_NONE    = -1;
    public static final int TYPE_SWORD   = 0; // épée
    public static final int TYPE_AXE     = 1; // hache
    public static final int TYPE_PICKAXE = 2; // pioche
    public static final int TYPE_SHOVEL  = 3; // pelle
    public static final int TYPE_HOE     = 4; // houe
    public static final int TYPE_BOW     = 5; // arc

    // Coût en énergie d'une action faite à la main ou avec le mauvais outil.
    public static final int BASE_ENERGY_COST = 5;

    /** True si l'ID correspond à un outil (plage 200-245). */
    public static boolean isTool(int id) {
        return id >= 200 && id <= 245;
    }

    /**
     * Tier de l'outil : 0 = aucun (main nue), 1 = bois, 2 = pierre,
     * 3 = fer, 4 = or, 5 = gemme.
     */
    public static int tier(int id) {
        if (!isTool(id)) return 0;
        return (id - 200) / 10 + 1;   // 200-209 -> 1, 210-219 -> 2, ...
    }

    /** Type de l'outil (épée, pioche, hache...) ou TYPE_NONE si ce n'en est pas un. */
    public static int type(int id) {
        if (!isTool(id)) return TYPE_NONE;
        return (id - 200) % 10;       // 0=épée, 1=hache, 2=pioche, ...
    }

    /**
     * Tier de PIOCHE minimum requis pour casser ce bloc.
     * 0 = aucune contrainte (le bloc se casse à la main).
     *
     * Règle du cahier des charges :
     *   Pierre  -> pioche bois (tier 1)
     *   Fer     -> pioche pierre (tier 2)
     *   Or      -> pioche fer (tier 3)
     *   Gemme   -> pioche or (tier 4)
     */
    public static int requiredPickaxeTier(int blockId) {
        switch (blockId) {
            case 5:  return 1; // Pierre (BLOCK_ROCK)
            case 47: return 1; // Minerai de charbon → pioche bois minimum
            case 36: return 2; // Minerai de fer
            case 37: return 3; // Minerai d'or
            case 38: return 4; // Minerai de gemme
            default: return 0; // tout le reste : pas de contrainte de tier
        }
    }

    /** True si ce bloc se mine à la pioche (pierre, minerais, dérivés de pierre). */
    public static boolean isMinable(int blockId) {
        if (requiredPickaxeTier(blockId) > 0) return true;
        switch (blockId) {
            case 9:  // mur de pierre
            case 12: // sol de pierre
            case 16: // barrière de pierre
            case 19: // porte de pierre
                return true;
            default:
                return false;
        }
    }

    /** True si ce bloc se casse à la hache (bois et dérivés). */
    public static boolean isChoppable(int blockId) {
        switch (blockId) {
            case 25: // Arbre
            case 10: // mur de bois
            case 13: // sol de bois
            case 15: // bois ornementé
            case 17: // barrière de bois
            case 21: // porte de bois
            case 22: // porte de bois ouverte
                return true;
            default:
                return false;
        }
    }

    /**
     * Le joueur a-t-il le BON outil pour casser ce bloc efficacement ?
     * (pioche sur pierre/minerai, hache sur bois)
     */
    public static boolean isCorrectTool(int blockId, int toolId) {
        return (isMinable(blockId)   && type(toolId) == TYPE_PICKAXE)
            || (isChoppable(blockId) && type(toolId) == TYPE_AXE);
    }

    /**
     * Coût en énergie pour casser `blockId` avec l'outil `toolId`.
     *  - Bon outil : le coût baisse avec le tier (bois=4, pierre=3, fer=2, or=1, gemme=0).
     *  - Mauvais outil ou main nue : coût de base (5).
     */
    public static int breakEnergyCost(int blockId, int toolId) {
        if (!isCorrectTool(blockId, toolId)) return BASE_ENERGY_COST;       // 5
        return Math.max(0, BASE_ENERGY_COST - tier(toolId));               // 4..0
    }

    /**
     * Coût en énergie d'une attaque selon l'épée tenue.
     * Main nue = 5 ; chaque tier d'épée réduit le coût (épée gemme = 0).
     */
    public static int attackEnergyCost(int toolId) {
        if (type(toolId) == TYPE_SWORD) {
            return Math.max(0, BASE_ENERGY_COST - tier(toolId));
        }
        return BASE_ENERGY_COST;
    }

    /**
     * Le joueur peut-il physiquement casser ce bloc avec l'outil tenu ?
     * Les blocs sous contrainte (pierre, minerais) exigent une pioche d'un tier suffisant ;
     * les autres blocs sont toujours cassables.
     */
    public static boolean canBreak(int blockId, int toolId) {
        int required = requiredPickaxeTier(blockId);
        if (required == 0) return true; // pas de contrainte
        return type(toolId) == TYPE_PICKAXE && tier(toolId) >= required;
    }
}
