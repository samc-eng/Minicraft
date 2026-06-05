package com.minicraft;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

// Test d'integration bout-en-bout :
// on cree un monde avec un joueur, des items, une grotte, on sauvegarde,
// puis on recharge dans un nouvel etat et on verifie que tout matche.
public class IntegrationTest {

    static class FakeMain extends Main {
        Level surface;
        Level[] underground = new Level[5];
        int depth;
        int[] portal;
        @Override public Level    getSurfaceLevel()      { return surface; }
        @Override public Level[]  getUndergroundLevels() { return underground; }
        @Override public int      getCurrentDepth()      { return depth; }
        @Override public int[]    getLastSurfacePortal() { return portal; }
    }

    private static final String TEST_SAVE = "test_integration_save.txt";

    @BeforeAll
    static void setupFx() {
        TestSetup.ensureFxStarted();
    }

    @AfterEach
    void cleanup() {
        File f = new File(TEST_SAVE);
        if (f.exists()) f.delete();
    }

    @Test
    @DisplayName("Round-trip complet : on sauvegarde, on recharge, l'etat est identique")
    void roundTrip_etatPreserve() throws Exception {
        // --- ETAT INITIAL ---
        Player joueur = new Player(512.0, 320.0);
        joueur.pickUpItem(new ItemStack(100, 17));        // 17 bois dans la hotbar
        joueur.pickUpItem(new ItemStack(101, 5));         // 5 pierres
        joueur.pickUpItem(new ItemStack(200, 1));         // 1 epee en bois
        joueur.getInventory().add(new ItemStack(102, 3)); // 3 charbons rangs dans l'inventaire
        joueur.setSelectedSlot(2);

        FakeMain engine = new FakeMain();
        engine.surface = new Level(60, 60, 0, 12345L);
        engine.depth = 0;
        engine.portal = null;

        // on modifie un bloc de surface pour verifier qu'il est restaure
        engine.surface.setBlocks(10 * Config.blockSize, 10 * Config.blockSize, 9); // mur de pierre
        int blocPosePos = engine.surface.getBlocks(10 * Config.blockSize, 10 * Config.blockSize);
        assertEquals(9, blocPosePos);

        // --- SAUVEGARDE ---
        SaveManager.saveGameToFile(joueur, engine, TEST_SAVE);
        assertTrue(new File(TEST_SAVE).exists());

        // --- RELECTURE DANS UN ETAT VIERGE ---
        Player joueurRecharge = new Player(0, 0);
        Level surfaceRechargee;
        Level caveRechargee = null;
        int depthRecharge;
        int[] portalRecharge;

        try (Scanner sc = SaveManager.getSaveScannerForFile(TEST_SAVE)) {
            assertNotNull(sc);

            // joueur
            String[] pData = sc.nextLine().split(",");
            joueurRecharge.setX(Double.parseDouble(pData[0]));
            joueurRecharge.setY(Double.parseDouble(pData[1]));

            // etat global
            String[] state = sc.nextLine().split(",");
            depthRecharge = Integer.parseInt(state[0]);
            int lpx = Integer.parseInt(state[1]);
            int lpy = Integer.parseInt(state[2]);
            portalRecharge = (lpx >= 0 && lpy >= 0) ? new int[]{lpx, lpy} : null;

            // surface
            surfaceRechargee = SaveManager.readLevel(sc);

            // grotte ?
            String cavePresent = sc.nextLine().trim();
            if (cavePresent.equals("1")) caveRechargee = SaveManager.readLevel(sc);

            // inventaire
            int nInv = Integer.parseInt(sc.nextLine().trim());
            for (int i = 0; i < nInv; i++) {
                String[] st = sc.nextLine().split(",");
                joueurRecharge.getInventory().add(new ItemStack(
                    Integer.parseInt(st[0]),
                    Integer.parseInt(st[1]),
                    Integer.parseInt(st[2])));
            }

            // hotbar
            String hbLine = sc.nextLine();
            String[] slots = hbLine.split(";", -1);
            ItemStack[] hb = joueurRecharge.getHotbar();
            for (int i = 0; i < slots.length && i < hb.length; i++) {
                String s = slots[i];
                if (s.equals("-") || s.isEmpty()) {
                    hb[i] = null;
                } else {
                    String[] parts = s.split(",");
                    hb[i] = new ItemStack(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]));
                }
            }
        }

        // --- VERIFICATIONS ---
        // position du joueur
        assertEquals(512.0, joueurRecharge.getX(), 0.001);
        assertEquals(320.0, joueurRecharge.getY(), 0.001);

        // etat global
        assertEquals(0, depthRecharge);
        assertNull(portalRecharge);

        // surface : meme dimensions, meme seed, et le bloc qu'on avait pose est toujours la
        assertEquals(60, surfaceRechargee.getWidth());
        assertEquals(60, surfaceRechargee.getHeight());
        assertEquals(12345L, surfaceRechargee.getSeed());
        assertEquals(9, surfaceRechargee.getBlocks(10 * Config.blockSize, 10 * Config.blockSize),
                     "le bloc pose avant la save doit etre la apres le rechargement");

        // pas de grotte
        assertNull(caveRechargee);

        // hotbar : verifie qu'on retrouve nos items dans les bonnes cases
        assertEquals(17, joueurRecharge.amountOf(100), "17 bois");
        assertEquals(5,  joueurRecharge.amountOf(101), "5 pierres");
        assertEquals(1,  joueurRecharge.amountOf(200), "epee");
        assertEquals(3,  joueurRecharge.amountOf(102), "3 charbons (dans le stockage)");
    }

    @Test
    @DisplayName("Round-trip avec une grotte : la grotte est preservee")
    void roundTrip_avecGrotte() throws Exception {
        Player joueur = new Player(100, 200);

        FakeMain engine = new FakeMain();
        engine.surface = new Level(60, 60, 0, 999L);
        engine.underground[0] = new Level(40, 40, 1, 999L);
        engine.depth = 1;
        engine.portal = new int[]{15, 20};

        // on casse un bloc dans la grotte
        engine.underground[0].setBlocks(5 * Config.blockSize, 5 * Config.blockSize, 0);

        SaveManager.saveGameToFile(joueur, engine, TEST_SAVE);

        // on rejoue le parsing
        Level surface = null, cave = null;
        int depth;
        int[] portal;
        try (Scanner sc = SaveManager.getSaveScannerForFile(TEST_SAVE)) {
            sc.nextLine(); // joueur
            String[] state = sc.nextLine().split(",");
            depth = Integer.parseInt(state[0]);
            portal = new int[]{Integer.parseInt(state[1]), Integer.parseInt(state[2])};
            surface = SaveManager.readLevel(sc);
            String cavePresent = sc.nextLine().trim();
            assertEquals("1", cavePresent, "le flag de grotte doit etre a 1");
            cave = SaveManager.readLevel(sc);
        }

        assertEquals(1, depth);
        assertEquals(15, portal[0]);
        assertEquals(20, portal[1]);
        assertNotNull(cave);
        assertEquals(40, cave.getWidth());
        assertEquals(999L, cave.getSeed());
        // le bloc qu'on a casse est toujours casse
        assertEquals(0, cave.getBlocks(5 * Config.blockSize, 5 * Config.blockSize));
    }

    @Test
    @DisplayName("Round-trip sans grotte : la save contient le flag 0 et pas de bloc grotte")
    void roundTrip_sansGrotte() throws Exception {
        Player joueur = new Player(0, 0);
        FakeMain engine = new FakeMain();
        engine.surface = new Level(40, 40, 0, 1L);

        SaveManager.saveGameToFile(joueur, engine, TEST_SAVE);

        try (Scanner sc = SaveManager.getSaveScannerForFile(TEST_SAVE)) {
            sc.nextLine(); sc.nextLine();
            SaveManager.readLevel(sc); // surface
            String cavePresent = sc.nextLine().trim();
            assertEquals("0", cavePresent);
        }
    }
}
