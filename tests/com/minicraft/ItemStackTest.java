package com.minicraft;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Tests unitaires pour ItemStack : construction, empilage, durabilité.
public class ItemStackTest {

    @BeforeAll
    static void setupFx() {
        TestSetup.ensureFxStarted();
    }

    @Test
    @DisplayName("Constructeur simple : amount et id corrects")
    void constructeur_idEtQuantite() {
        ItemStack s = new ItemStack(100, 5); // 100 = bois
        assertEquals(100, s.getItemId());
        assertEquals(5, s.getAmount());
    }

    @Test
    @DisplayName("Constructeur simple : durabilité initialisée au max pour un outil")
    void constructeur_durabiliteParDefaut() {
        ItemStack epee = new ItemStack(200, 1); // épée en bois, maxDurability = 60
        assertEquals(60, epee.getCurrentDurability());
    }

    @Test
    @DisplayName("Constructeur simple : durabilité = 0 pour un item sans durabilité")
    void constructeur_durabiliteZeroSansOutil() {
        ItemStack bois = new ItemStack(100, 10);
        assertEquals(0, bois.getCurrentDurability());
    }

    @Test
    @DisplayName("Constructeur avec durabilité : valeur exacte restaurée")
    void constructeur_durabiliteForcee() {
        ItemStack epeeUsee = new ItemStack(200, 1, 17);
        assertEquals(17, epeeUsee.getCurrentDurability());
    }

    @Test
    @DisplayName("add augmente la quantité")
    void add_augmenteAmount() {
        ItemStack s = new ItemStack(100, 5);
        s.add(3);
        assertEquals(8, s.getAmount());
    }

    @Test
    @DisplayName("remove diminue la quantité")
    void remove_diminueAmount() {
        ItemStack s = new ItemStack(100, 5);
        s.remove(2);
        assertEquals(3, s.getAmount());
    }

    @Test
    @DisplayName("isFull est vrai quand amount atteint maxStack")
    void isFull_quandPileMaximale() {
        ItemStack s = new ItemStack(100, 64); // maxStack = 64 pour le bois
        assertTrue(s.isFull());
    }

    @Test
    @DisplayName("isFull est faux quand il reste de la place")
    void isFull_quandPileNonRemplie() {
        ItemStack s = new ItemStack(100, 30);
        assertFalse(s.isFull());
    }

    @Test
    @DisplayName("damage diminue la durabilité")
    void damage_diminueDurabilite() {
        ItemStack epee = new ItemStack(200, 1);
        epee.damage(10);
        assertEquals(50, epee.getCurrentDurability());
    }

    @Test
    @DisplayName("isBroken est vrai quand la durabilité tombe à 0")
    void isBroken_quandDurabiliteEpuisee() {
        ItemStack epee = new ItemStack(200, 1);
        epee.damage(60);
        assertTrue(epee.isBroken());
    }

    @Test
    @DisplayName("isBroken est faux pour un item sans durabilité")
    void isBroken_faux_pourRessource() {
        ItemStack bois = new ItemStack(100, 5);
        assertFalse(bois.isBroken());
    }

    @Test
    @DisplayName("canStackWith est vrai pour deux items identiques empilables")
    void canStackWith_memeIdEmpilable() {
        ItemStack a = new ItemStack(100, 5);
        ItemStack b = new ItemStack(100, 10);
        assertTrue(a.canStackWith(b));
    }

    @Test
    @DisplayName("canStackWith est faux pour deux ids différents")
    void canStackWith_idsDifferents() {
        ItemStack bois  = new ItemStack(100, 5);
        ItemStack pierre = new ItemStack(101, 5);
        assertFalse(bois.canStackWith(pierre));
    }

    @Test
    @DisplayName("canStackWith est faux pour un outil (maxStack = 1)")
    void canStackWith_outilNonEmpilable() {
        ItemStack epee1 = new ItemStack(200, 1);
        ItemStack epee2 = new ItemStack(200, 1);
        assertFalse(epee1.canStackWith(epee2));
    }

    @Test
    @DisplayName("getDefinition renvoie la bonne ItemDefinition")
    void getDefinition_renvoieDefinition() {
        ItemStack bois = new ItemStack(100, 5);
        ItemDefinition def = bois.getDefinition();
        assertNotNull(def);
        assertEquals(100, def.id);
    }
}
