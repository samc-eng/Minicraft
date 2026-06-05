package com.minicraft;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Tests pour la classe Recipe : canCraft et Craft.
public class RecipeTest {

    private Player player;

    @BeforeAll
    static void setupFx() {
        TestSetup.ensureFxStarted();
    }

    @BeforeEach
    void freshPlayer() {
        player = new Player(0, 0);
    }

    @Test
    @DisplayName("canCraft est vrai quand le joueur a tous les ingrédients")
    void canCraft_ingredientsPresents() {
        Recipe etabli = new Recipe("Etabli", 28, 1).addCost(100, 5);
        player.pickUpItem(new ItemStack(100, 5));
        assertTrue(etabli.canCraft(player));
    }

    @Test
    @DisplayName("canCraft est faux quand il manque un ingrédient")
    void canCraft_ingredientManquant() {
        Recipe etabli = new Recipe("Etabli", 28, 1).addCost(100, 5);
        player.pickUpItem(new ItemStack(100, 3));
        assertFalse(etabli.canCraft(player));
    }

    @Test
    @DisplayName("canCraft regarde aussi dans l'inventaire (pas que la hotbar)")
    void canCraft_regardeInventaire() {
        Recipe etabli = new Recipe("Etabli", 28, 1).addCost(100, 5);
        // ressources dans l'inventaire seulement
        player.getInventory().add(new ItemStack(100, 5));
        assertTrue(etabli.canCraft(player));
    }

    @Test
    @DisplayName("canCraft vérifie plusieurs ingrédients à la fois")
    void canCraft_plusieursIngredients() {
        Recipe torche = new Recipe("Torche", 27, 4).addCost(100, 1).addCost(102, 1);
        player.pickUpItem(new ItemStack(100, 1));
        // il manque encore le charbon (102)
        assertFalse(torche.canCraft(player));

        player.pickUpItem(new ItemStack(102, 1));
        assertTrue(torche.canCraft(player));
    }

    @Test
    @DisplayName("Craft consomme les ingrédients et donne le résultat")
    void craft_consommeEtDonne() {
        Recipe etabli = new Recipe("Etabli", 28, 1).addCost(100, 5);
        player.pickUpItem(new ItemStack(100, 5));

        etabli.Craft(player);

        // bois consommé
        assertEquals(0, player.amountOf(100));
        // établi obtenu (1 unité)
        assertEquals(1, player.amountOf(28));
    }

    @Test
    @DisplayName("Craft ne fait rien si les ingrédients ne sont pas là")
    void craft_neFaitRienSiInsuffisant() {
        Recipe etabli = new Recipe("Etabli", 28, 1).addCost(100, 5);
        player.pickUpItem(new ItemStack(100, 3));

        etabli.Craft(player);

        // ressources intactes, pas d'établi créé
        assertEquals(3, player.amountOf(100));
        assertEquals(0, player.amountOf(28));
    }

    @Test
    @DisplayName("Craft d'une recette x4 produit bien 4 unités")
    void craft_multipleUnits() {
        Recipe torche = new Recipe("Torche x4", 27, 4).addCost(100, 1).addCost(102, 1);
        player.pickUpItem(new ItemStack(100, 1));
        player.pickUpItem(new ItemStack(102, 1));

        torche.Craft(player);

        assertEquals(4, player.amountOf(27), "torche est x4");
    }

    @Test
    @DisplayName("Craft consomme correctement à travers hotbar + inventaire")
    void craft_consomme_entreHotbarEtInventaire() {
        Recipe etabli = new Recipe("Etabli", 28, 1).addCost(100, 5);
        // 3 dans la hotbar
        player.pickUpItem(new ItemStack(100, 3));
        // 2 dans l'inventaire
        player.getInventory().add(new ItemStack(100, 2));

        etabli.Craft(player);

        assertEquals(0, player.amountOf(100), "tout le bois doit être consommé");
        assertEquals(1, player.amountOf(28));
    }
}
