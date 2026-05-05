package com.minicraft;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
public class DropTable {
    private static final Map<Integer, List<Drop>> table = new HashMap<>();
 
    static {
        // Blocs de base → drop eux-mêmes
        register(1,  new Drop(1,   1, 1, 1.0));  // Grass
        register(2,  new Drop(2,   1, 1, 1.0));  // Dirt
        register(3,  new Drop(3,   1, 1, 1.0));  // Gray Dirt
        register(4,  new Drop(4,   1, 1, 1.0));  // Sand
 
        // Pierre
        register(5,  new Drop(101, 1, 3, 1.0));  // Stone
        register(6,  new Drop(101, 2, 4, 1.0));  // Rock
        register(7,  new Drop(101, 3, 5, 1.0));  // Hard Rock
 
        // Arbre
        register(25, new Drop(100, 1, 3, 1.0));  // Tree → Wood
        register(25, new Drop(115, 0, 1, 0.2));  // Tree → Acorn (20%)
 
        // Minerais
        register(36, new Drop(103, 1, 2, 1.0));  // Iron Ore
        register(37, new Drop(104, 1, 2, 1.0));  // Gold Ore
        register(38, new Drop(107, 1, 1, 1.0));  // Gem Ore
        register(39, new Drop(108, 1, 3, 1.0));  // Lapis Ore
 
        // Plantes
        register(26, new Drop(128, 1, 2, 1.0));  // Cactus
        register(46, new Drop(112, 1, 3, 1.0));  // Carrot Stage 3
    }
 
    private static void register(int blockId, Drop drop) {
        table.computeIfAbsent(blockId, k -> new ArrayList<>()).add(drop);
    }
 
    // Retourne la liste des drops pour un bloc donné
    public static List<Drop> get(int blockId) {
        return table.getOrDefault(blockId, Collections.emptyList());
    }
}

