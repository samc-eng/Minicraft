package com.minicraft;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
    private static final Map<Integer, ItemDefinition> catalogue = new HashMap<>();

    static {
        // BLOCS (stackables, posables, pas de durabilité)
        register(new ItemDefinition(1, "Pierre",       64, true,  false, 0, "blocks/stone.png"));
        register(new ItemDefinition(2, "Mur",          64, true,  false, 0, "blocks/rock.png"));
        register(new ItemDefinition(3, "Planche",      64, true,  false, 0, "Blocks/"));

        // RESSOURCES (stackables, pas posables, pas de durabilité)
        register(new ItemDefinition(10, "Bâton",       64, false, false, 0, "items/"));
        register(new ItemDefinition(11, "Charbon",     64, false, false, 0,"items/"));

        // OUTILS (pas stackables, durabilité)
        register(new ItemDefinition(12, "Pioche bois", 1,  false, true,  60, "tools/"));
        register(new ItemDefinition(13, "Pioche fer",  1,  false, true,  250, "tools/"));
    }

    private static void register(ItemDefinition def) {
        catalogue.put(def.id, def);
    }

    public static ItemDefinition get(int id) {
        return catalogue.get(id);
    }

    public static boolean exists(int id) {
        return catalogue.containsKey(id);
    }
}
