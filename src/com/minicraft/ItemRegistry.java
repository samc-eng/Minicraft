package com.minicraft;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
    private static final Map<Integer, ItemDefinition> catalogue = new HashMap<>();

    static {
        // =====================
        // BLOCS (1-99)
        // =====================
        register(new ItemDefinition(1,  "Grass",              64, true,  false, 0, "blocks/grass.png"));
        register(new ItemDefinition(2,  "Dirt",               64, true,  false, 0, "blocks/dirt.png"));
        register(new ItemDefinition(3,  "Gray Dirt",          64, true,  false, 0, "blocks/gray_dirt.png"));
        register(new ItemDefinition(4,  "Sand",               64, true,  false, 0, "blocks/sand.png"));
        register(new ItemDefinition(5,  "Stone",              64, true,  false, 0, "blocks/stone.png"));
        register(new ItemDefinition(6,  "Rock",               64, true,  false, 0, "blocks/rock.png"));
        register(new ItemDefinition(7,  "Hard Rock",          64, true,  false, 0, "blocks/hardrock.png"));
        register(new ItemDefinition(8,  "Obsidian",           64, true,  false, 0, "blocks/obsidian.png"));
        register(new ItemDefinition(9,  "Stone Wall",         64, true,  false, 0, "blocks/stone_wall.png"));
        register(new ItemDefinition(10, "Wood Wall",          64, true,  false, 0, "blocks/wood_wall.png"));
        register(new ItemDefinition(11, "Obsidian Wall",      64, true,  false, 0, "blocks/obsidian_wall.png"));
        register(new ItemDefinition(12, "Stone Floor",        64, true,  false, 0, "blocks/ornate_stone.png"));
        register(new ItemDefinition(13, "Wood Floor",         64, true,  false, 0, "blocks/wood_floor.png"));
        register(new ItemDefinition(14, "Obsidian Floor",     64, true,  false, 0, "blocks/obsidian_floor.png"));
        register(new ItemDefinition(15, "Ornate Wood",        64, true,  false, 0, "blocks/ornate_wood.png"));
        register(new ItemDefinition(16, "Stone Fence",        64, true,  false, 0, "blocks/stone_fence.png"));
        register(new ItemDefinition(17, "Wood Fence",         64, true,  false, 0, "blocks/wood_fence.png"));
        register(new ItemDefinition(18, "Obsidian Fence",     64, true,  false, 0, "blocks/obsidian_fence.png"));
        register(new ItemDefinition(19, "Stone Door",         64, true,  false, 0, "blocks/stone_door.png"));
        register(new ItemDefinition(20, "Stone Door Opened",  64, true,  false, 0, "blocks/stone_door_opened.png"));
        register(new ItemDefinition(21, "Wood Door",          64, true,  false, 0, "blocks/wood_door.png"));
        register(new ItemDefinition(22, "Wood Door Opened",   64, true,  false, 0, "blocks/wood_door_opened.png"));
        register(new ItemDefinition(23, "Obsidian Door",      64, true,  false, 0, "blocks/obsidian_door.png"));
        register(new ItemDefinition(24, "Obsidian Door Opened",64, true, false, 0, "blocks/obsidian_door_opened.png"));
        register(new ItemDefinition(25, "Tree",               64, true,  false, 0, "blocks/tree.png"));
        register(new ItemDefinition(26, "Cactus",             64, true,  false, 0, "blocks/cactus.png"));
        register(new ItemDefinition(27, "Torch",              64, true,  false, 0, "blocks/torch.png"));
        register(new ItemDefinition(28, "Workbench",          64, true,  false, 0, "blocks/workbench.png"));
        register(new ItemDefinition(29, "Water",              64, true,  false, 0, "blocks/water.png"));
        register(new ItemDefinition(30, "Water Border",       64, true,  false, 0, "blocks/water_border.png"));
        register(new ItemDefinition(31, "Lava",               64, true,  false, 0, "blocks/lava.png"));
        register(new ItemDefinition(32, "Hole",               64, true,  false, 0, "blocks/hole.png"));
        register(new ItemDefinition(33, "Hole Border",        64, true,  false, 0, "blocks/hole_border.png"));
        register(new ItemDefinition(34, "Sand Border",        64, true,  false, 0, "blocks/sand_border.png"));
        register(new ItemDefinition(35, "Path",               64, true,  false, 0, "blocks/path.png"));
        register(new ItemDefinition(36, "Iron Ore",           64, true,  false, 0, "blocks/iron_ore.png"));
        register(new ItemDefinition(37, "Gold Ore",           64, true,  false, 0, "blocks/gold_ore.png"));
        register(new ItemDefinition(38, "Gem Ore",            64, true,  false, 0, "blocks/gem_ore.png"));
        register(new ItemDefinition(39, "Lapis Ore",          64, true,  false, 0, "blocks/lapis_ore.png"));
        register(new ItemDefinition(40, "Iris",               64, true,  false, 0, "blocks/iris.png"));
        register(new ItemDefinition(41, "Stairs Up",          64, true,  false, 0, "blocks/stairs_up.png"));
        register(new ItemDefinition(42, "Stairs Down",        64, true,  false, 0, "blocks/stairs_down.png"));
        register(new ItemDefinition(43, "Carrot Stage 0",     64, true,  false, 0, "blocks/carrot_stage0.png"));
        register(new ItemDefinition(44, "Carrot Stage 1",     64, true,  false, 0, "blocks/carrot_stage1.png"));
        register(new ItemDefinition(45, "Carrot Stage 2",     64, true,  false, 0, "blocks/carrot_stage2.png"));
        register(new ItemDefinition(46, "Carrot Stage 3",     64, true,  false, 0, "blocks/carrot_stage3.png"));
        register(new ItemDefinition(99, "Missing Tile",       64, true,  false, 0, "blocks/missing_tile.png"));
 
        // =====================
        // ITEMS (100-199)
        // =====================
        register(new ItemDefinition(100, "Wood",           64, false, false, 0, "items/wood.png"));
        register(new ItemDefinition(101, "Stone Item",     64, false, false, 0, "blocks/stone.png"));
        register(new ItemDefinition(102, "Coal",           64, false, false, 0, "items/coal.png"));
        register(new ItemDefinition(103, "Iron Ore Item",  64, false, false, 0, "items/iron_ore_item.png"));
        register(new ItemDefinition(104, "Gold Ore Item",  64, false, false, 0, "items/gold_ore_item.png"));
        register(new ItemDefinition(105, "Iron Ingot",     64, false, false, 0, "items/iron_ingot.png"));
        register(new ItemDefinition(106, "Gold Ingot",     64, false, false, 0, "items/gold_ingot.png"));
        register(new ItemDefinition(107, "Gem",            64, false, false, 0, "items/gem.png"));
        register(new ItemDefinition(108, "Lapis",          64, false, false, 0, "items/lapis.png"));
        register(new ItemDefinition(109, "Obsidian Item",  64, false, false, 0, "items/obsidian_item.png"));
        register(new ItemDefinition(110, "Apple",          64, false, false, 0, "items/apple.png"));
        register(new ItemDefinition(111, "Golden Apple",   64, false, false, 0, "items/golden_apple.png"));
        register(new ItemDefinition(112, "Carrot",         64, false, false, 0, "items/carrot.png"));
        register(new ItemDefinition(113, "Beef",           64, false, false, 0, "items/beef.png"));
        register(new ItemDefinition(114, "Cooked Beef",    64, false, false, 0, "items/cooked_beef.png"));
        register(new ItemDefinition(115, "Acorn",          64, false, false, 0, "items/acorn.png"));
        register(new ItemDefinition(116, "Bone",           64, false, false, 0, "items/bone.png"));
        register(new ItemDefinition(117, "Bone Meal",      64, false, false, 0, "items/bone_meal.png"));
        register(new ItemDefinition(118, "Arrow",          64, false, false, 0, "items/arrow.png"));
        register(new ItemDefinition(119, "Bucket",         64, false, false, 0, "items/bucket.png"));
        register(new ItemDefinition(120, "Water Bucket",   1,  false, false, 0, "items/water_bucket.png"));
        register(new ItemDefinition(121, "Lava Bucket",    1,  false, false, 0, "items/lava_bucket.png"));
        register(new ItemDefinition(122, "Chest",          64, true,  false, 0, "items/chest.png"));
        register(new ItemDefinition(123, "Boat",           1,  false, false, 0, "items/boat.png"));
        register(new ItemDefinition(124, "Potion",         64, false, false, 0, "items/potion.png"));
        register(new ItemDefinition(125, "Book",           64, false, false, 0, "items/book.png"));
        register(new ItemDefinition(126, "Antidious Book", 1,  false, false, 0, "items/antidious_book.png"));
        register(new ItemDefinition(127, "Air Totem",      1,  false, false, 0, "items/air_totem.png"));
        register(new ItemDefinition(128, "Cactus Item",    64, false, false, 0, "items/cactus_item.png"));
        register(new ItemDefinition(129, "Black Wool",     64, false, false, 0, "items/black_wool.png"));
        register(new ItemDefinition(130, "Black Bed",      1,  false, false, 0, "items/black_bed.png"));
 
        // =====================
        // TOOLS (200-299)
        // =====================
        // Bois
        register(new ItemDefinition(200, "Wooden Sword",      1, false, true, 60,  "tools/wooden_sword.png"));
        register(new ItemDefinition(201, "Wooden Axe",        1, false, true, 60,  "tools/wooden_axe.png"));
        register(new ItemDefinition(202, "Wooden Pickaxe",    1, false, true, 60,  "tools/wooden_pickaxe.png"));
        register(new ItemDefinition(203, "Wooden Shovel",     1, false, true, 60,  "tools/wooden_shovel.png"));
        register(new ItemDefinition(204, "Wooden Hoe",        1, false, true, 60,  "tools/wooden_hoe.png"));
        register(new ItemDefinition(205, "Wooden Bow",        1, false, true, 60,  "tools/wooden_bow.png"));
        register(new ItemDefinition(206, "Wooden Fishing Rod",1, false, true, 60,  "tools/wooden_fishing_rod.png"));
        // Pierre
        register(new ItemDefinition(210, "Stone Sword",       1, false, true, 120, "tools/stone_sword.png"));
        register(new ItemDefinition(211, "Stone Axe",         1, false, true, 120, "tools/stone_axe.png"));
        register(new ItemDefinition(212, "Stone Pickaxe",     1, false, true, 120, "tools/stone_pickaxe.png"));
        register(new ItemDefinition(213, "Stone Shovel",      1, false, true, 120, "tools/stone_shovel.png"));
        register(new ItemDefinition(214, "Stone Hoe",         1, false, true, 120, "tools/stone_hoe.png"));
        register(new ItemDefinition(215, "Stone Bow",         1, false, true, 120, "tools/stone_bow.png"));
        // Fer
        register(new ItemDefinition(220, "Iron Sword",        1, false, true, 250, "tools/iron_sword.png"));
        register(new ItemDefinition(221, "Iron Axe",          1, false, true, 250, "tools/iron_axe.png"));
        register(new ItemDefinition(222, "Iron Pickaxe",      1, false, true, 250, "tools/iron_pickaxe.png"));
        register(new ItemDefinition(223, "Iron Shovel",       1, false, true, 250, "tools/iron_shovel.png"));
        register(new ItemDefinition(224, "Iron Hoe",          1, false, true, 250, "tools/iron_hoe.png"));
        register(new ItemDefinition(225, "Iron Bow",          1, false, true, 250, "tools/iron_bow.png"));
        // Or
        register(new ItemDefinition(230, "Gold Sword",        1, false, true, 180, "tools/gold_sword.png"));
        register(new ItemDefinition(231, "Gold Axe",          1, false, true, 180, "tools/gold_axe.png"));
        register(new ItemDefinition(232, "Gold Pickaxe",      1, false, true, 180, "tools/gold_pickaxe.png"));
        register(new ItemDefinition(233, "Gold Shovel",       1, false, true, 180, "tools/gold_shovel.png"));
        register(new ItemDefinition(234, "Gold Hoe",          1, false, true, 180, "tools/gold_hoe.png"));
        register(new ItemDefinition(235, "Gold Bow",          1, false, true, 180, "tools/gold_bow.png"));
        // Gemme
        register(new ItemDefinition(240, "Gem Sword",         1, false, true, 500, "tools/gem_sword.png"));
        register(new ItemDefinition(241, "Gem Axe",           1, false, true, 500, "tools/gem_axe.png"));
        register(new ItemDefinition(242, "Gem Pickaxe",       1, false, true, 500, "tools/gem_pickaxe.png"));
        register(new ItemDefinition(243, "Gem Shovel",        1, false, true, 500, "tools/gem_shovel.png"));
        register(new ItemDefinition(244, "Gem Hoe",           1, false, true, 500, "tools/gem_hoe.png"));
        register(new ItemDefinition(245, "Gem Bow",           1, false, true, 500, "tools/gem_bow.png"));
        // =====================
        // ARMURES (300-399) : 1 piece par materiau
        // Fer : 300 | Or : 310 | Gem : 320
        // =====================

        // Fer
        register(new ItemDefinition(300, "Armure en fer",    1, false, true, 200, "tools/iron_armor.png"));
   
        // Or
        register(new ItemDefinition(310, "Armure en or",    1, false, true, 200, "tools/gold_armor.png"));

        // Gem
        register(new ItemDefinition(320, "Armure en gem",    1, false, true, 200, "tools/gem_armor.png"));

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
