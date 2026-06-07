package com.minicraft;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Tests unitaires de la logique unifiée hotbar / inventaire sur le Player.
// On teste pickUpItem, amountOf, has, consume.
public class PlayerTest {

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
    @DisplayName("Player neuf : hotbar vide, amountOf = 0 partout")
    void playerNeuf_inventaireVide() {
        for (ItemStack s : player.getHotbar()) {
            assertNull(s);
        }
        assertEquals(0, player.amountOf(100));
    }

    @Test
    @DisplayName("pickUpItem : un item va dans la 1ere case vide de la hotbar")
    void pickUp_remplitHotbarEnPremier() {
        player.pickUpItem(new ItemStack(100, 5));
        assertNotNull(player.getHotbar()[0]);
        assertEquals(100, player.getHotbar()[0].getItemId());
        assertEquals(5, player.getHotbar()[0].getAmount());
        // pas de duplication : l'inventaire reste vide
        assertTrue(player.getInventory().getAll().isEmpty(),
                "Pas de duplication entre hotbar et inventaire");
    }

    @Test
    @DisplayName("pickUpItem : deux pickups du même item empilent dans la même case")
    void pickUp_empileSurCaseExistante() {
        player.pickUpItem(new ItemStack(100, 5));
        player.pickUpItem(new ItemStack(100, 3));
        assertEquals(8, player.getHotbar()[0].getAmount());
        assertNull(player.getHotbar()[1], "Le 2e pickup aurait dû empiler, pas créer une nouvelle case");
    }

    @Test
    @DisplayName("pickUpItem : quand la hotbar est pleine, l'item part dans l'inventaire")
    void pickUp_hotbarPleine_vaDansInventaire() {
        // on remplit les 9 slots avec des outils différents (non empilables)
        int[] ids = {200, 201, 202, 203, 205, 210, 211, 212, 213};
        for (int id : ids) {
            player.pickUpItem(new ItemStack(id, 1));
        }
        // un 10e item doit aller dans l'inventaire
        player.pickUpItem(new ItemStack(220, 1));
        assertEquals(1, player.getInventory().getAll().size());
        assertEquals(220, player.getInventory().getAll().get(0).getItemId());
    }

    @Test
    @DisplayName("amountOf compte la hotbar et l'inventaire")
    void amountOf_compteLesDeux() {
        // 5 bois dans la hotbar
        player.pickUpItem(new ItemStack(100, 5));
        // 3 bois ajoutés directement à l'inventaire
        player.getInventory().add(new ItemStack(100, 3));
        assertEquals(8, player.amountOf(100));
    }

    @Test
    @DisplayName("has est vrai si le total hotbar+inventaire suffit")
    void has_totalSuffisant() {
        player.pickUpItem(new ItemStack(100, 3));
        player.getInventory().add(new ItemStack(100, 4));
        assertTrue(player.has(100, 7));
        assertTrue(player.has(100, 5));
        assertFalse(player.has(100, 8));
    }

    @Test
    @DisplayName("consume retire d'abord depuis la hotbar")
    void consume_videLaHotbarEnPremier() {
        player.pickUpItem(new ItemStack(100, 5));
        player.getInventory().add(new ItemStack(100, 5));
        assertTrue(player.consume(100, 3));
        // 2 doivent rester dans la hotbar, inventaire intact
        assertEquals(2, player.getHotbar()[0].getAmount());
        assertEquals(5, player.getInventory().getAmount(100));
    }

    @Test
    @DisplayName("consume bascule sur l'inventaire si la hotbar ne suffit pas")
    void consume_basculesurInventaireSiBesoin() {
        player.pickUpItem(new ItemStack(100, 3));
        player.getInventory().add(new ItemStack(100, 5));
        assertTrue(player.consume(100, 6));
        // hotbar vidée (3 pris) + 3 pris dans l'inventaire = il en reste 2
        assertNull(player.getHotbar()[0]);
        assertEquals(2, player.getInventory().getAmount(100));
    }

    @Test
    @DisplayName("consume renvoie false et ne touche à rien si insuffisant")
    void consume_insuffisant_renvoieFaux() {
        player.pickUpItem(new ItemStack(100, 2));
        assertFalse(player.consume(100, 5));
        // état inchangé
        assertEquals(2, player.getHotbar()[0].getAmount());
    }

    @Test
    @DisplayName("consume qui vide une case la met à null")
    void consume_videUneCase_metANull() {
        player.pickUpItem(new ItemStack(100, 5));
        assertTrue(player.consume(100, 5));
        assertNull(player.getHotbar()[0]);
    }

    @Test
    @DisplayName("setSelectedSlot accepte 0..8 et rejette les autres")
    void setSelectedSlot_borneRespectees() {
        player.setSelectedSlot(5);
        assertEquals(5, player.getSelectedSlot());

        player.setSelectedSlot(-1);
        assertEquals(5, player.getSelectedSlot(), "valeur invalide ignorée");

        player.setSelectedSlot(9);
        assertEquals(5, player.getSelectedSlot(), "valeur invalide ignorée");
    }
}
