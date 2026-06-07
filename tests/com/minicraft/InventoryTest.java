package com.minicraft;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Tests unitaires pour Inventory : ajout, retrait, comptage, empilage.
public class InventoryTest {

    private Inventory inv;

    @BeforeAll
    static void setupFx() {
        TestSetup.ensureFxStarted();
    }

    @BeforeEach
    void freshInventory() {
        inv = new Inventory();
    }

    @Test
    @DisplayName("Inventaire vide : getAmount renvoie 0")
    void inventaireVide() {
        assertEquals(0, inv.getAmount(100));
        assertTrue(inv.getAll().isEmpty());
    }

    @Test
    @DisplayName("add d'un item : getAmount renvoie la quantité")
    void add_unItem() {
        inv.add(new ItemStack(100, 5));
        assertEquals(5, inv.getAmount(100));
    }

    @Test
    @DisplayName("add de deux piles du même item empilable : elles fusionnent")
    void add_memeItemEmpilable_fusionne() {
        inv.add(new ItemStack(100, 5));
        inv.add(new ItemStack(100, 3));
        assertEquals(8, inv.getAmount(100));
        assertEquals(1, inv.getAll().size(), "Les piles auraient dû fusionner");
    }

    @Test
    @DisplayName("add de plusieurs items différents : chaque pile est conservée")
    void add_itemsDifferents() {
        inv.add(new ItemStack(100, 5));
        inv.add(new ItemStack(101, 3));
        assertEquals(5, inv.getAmount(100));
        assertEquals(3, inv.getAmount(101));
        assertEquals(2, inv.getAll().size());
    }

    @Test
    @DisplayName("add de deux outils (non empilables) : deux entrées distinctes")
    void add_outils_nonEmpilables() {
        inv.add(new ItemStack(200, 1));
        inv.add(new ItemStack(200, 1));
        // les outils ont maxStack = 1, donc ne s'empilent pas
        assertEquals(2, inv.getAll().size());
    }

    @Test
    @DisplayName("has est vrai quand on a assez")
    void has_quantiteSuffisante() {
        inv.add(new ItemStack(100, 10));
        assertTrue(inv.has(100, 5));
        assertTrue(inv.has(100, 10));
    }

    @Test
    @DisplayName("has est faux quand il en manque")
    void has_quantiteInsuffisante() {
        inv.add(new ItemStack(100, 3));
        assertFalse(inv.has(100, 5));
    }

    @Test
    @DisplayName("has est faux pour un item absent")
    void has_itemAbsent() {
        assertFalse(inv.has(100, 1));
    }

    @Test
    @DisplayName("remove diminue la quantité")
    void remove_diminueQuantite() {
        inv.add(new ItemStack(100, 10));
        inv.remove(100, 3);
        assertEquals(7, inv.getAmount(100));
    }

    @Test
    @DisplayName("remove qui vide complètement supprime la pile")
    void remove_videPile() {
        inv.add(new ItemStack(100, 5));
        inv.remove(100, 5);
        assertEquals(0, inv.getAmount(100));
        assertTrue(inv.getAll().isEmpty(), "La pile vide aurait dû être supprimée");
    }

    @Test
    @DisplayName("remove qui couvre plusieurs piles consomme correctement")
    void remove_traverse_plusieursPiles() {
        // deux outils dans des piles distinctes
        inv.add(new ItemStack(200, 1));
        inv.add(new ItemStack(200, 1));
        assertTrue(inv.remove(200, 2));
        assertEquals(0, inv.getAmount(200));
    }

    @Test
    @DisplayName("getAll renvoie la liste réelle des piles")
    void getAll_renvoieToutesLesPiles() {
        inv.add(new ItemStack(100, 5));
        inv.add(new ItemStack(101, 2));
        List<ItemStack> all = inv.getAll();
        assertEquals(2, all.size());
    }
}
