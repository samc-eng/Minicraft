package com.minicraft;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

// Tests de SaveManager. On utilise un fichier temporaire pour ne pas écraser une vraie sauvegarde.
public class SaveManagerTest {

    // un Main simplifié qui retourne ce qu'on lui donne, sans passer par launchGame
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

    private static final String TEST_SAVE = "test_save_tmp.txt";

    private Player player;
    private FakeMain engine;

    @BeforeAll
    static void setupFx() {
        TestSetup.ensureFxStarted();
    }

    @BeforeEach
    void freshState() {
        player = new Player(123.0, 456.0);
        engine = new FakeMain();
        // un petit niveau de surface pour limiter la taille du fichier généré
        engine.surface = new Level(60, 60, 0, 42L);
    }

    @AfterEach
    void cleanup() {
        File f = new File(TEST_SAVE);
        if (f.exists()) f.delete();
    }

    @Test
    @DisplayName("saveGameToFile crée un fichier non vide")
    void save_creeLeFichier() {
        SaveManager.saveGameToFile(player, engine, TEST_SAVE);
        File f = new File(TEST_SAVE);
        assertTrue(f.exists(), "le fichier de save doit exister");
        assertTrue(f.length() > 0, "le fichier ne doit pas être vide");
    }

    @Test
    @DisplayName("La première ligne contient la position du joueur")
    void save_premiereLigne_estPosition() throws Exception {
        SaveManager.saveGameToFile(player, engine, TEST_SAVE);
        try (Scanner sc = new Scanner(new File(TEST_SAVE))) {
            String pLine = sc.nextLine();
            String[] xy = pLine.split(",");
            assertEquals(123.0, Double.parseDouble(xy[0]), 0.001);
            assertEquals(456.0, Double.parseDouble(xy[1]), 0.001);
        }
    }

    @Test
    @DisplayName("La 2e ligne contient currentDepth et le portail")
    void save_deuxiemeLigne_etatGlobal() throws Exception {
        engine.depth = 0;
        engine.portal = null;
        SaveManager.saveGameToFile(player, engine, TEST_SAVE);
        try (Scanner sc = new Scanner(new File(TEST_SAVE))) {
            sc.nextLine(); // ignore la position
            String[] state = sc.nextLine().split(",");
            assertEquals(0, Integer.parseInt(state[0]));
            assertEquals(-1, Integer.parseInt(state[1]), "portal=null encodé -1");
            assertEquals(-1, Integer.parseInt(state[2]));
        }
    }

    @Test
    @DisplayName("Le portail est sauvegardé quand on en a un")
    void save_portailNonNull_estEcrit() throws Exception {
        engine.depth = 1;
        engine.portal = new int[]{17, 42};
        SaveManager.saveGameToFile(player, engine, TEST_SAVE);
        try (Scanner sc = new Scanner(new File(TEST_SAVE))) {
            sc.nextLine();
            String[] state = sc.nextLine().split(",");
            assertEquals(1, Integer.parseInt(state[0]));
            assertEquals(17, Integer.parseInt(state[1]));
            assertEquals(42, Integer.parseInt(state[2]));
        }
    }

    @Test
    @DisplayName("L'inventaire est sauvegardé avec id, quantité, durabilité")
    void save_inventaire_estEcrit() throws Exception {
        // on remplit la hotbar avec quelque chose puis on en met dans l'inventaire
        player.getInventory().add(new ItemStack(100, 12, 0));
        player.getInventory().add(new ItemStack(200, 1, 35));

        SaveManager.saveGameToFile(player, engine, TEST_SAVE);

        try (Scanner sc = new Scanner(new File(TEST_SAVE))) {
            // on saute les 2 premières lignes (joueur + état)
            sc.nextLine(); sc.nextLine();
            // saute la surface : 1 ligne dims + sol + blocs + nItems + nBots
            sc.nextLine(); // dims
            sc.nextLine(); // floor
            sc.nextLine(); // blocks
            int nItems = Integer.parseInt(sc.nextLine().trim());
            for (int i = 0; i < nItems; i++) sc.nextLine();
            int nBots = Integer.parseInt(sc.nextLine().trim());
            for (int i = 0; i < nBots; i++) sc.nextLine();
            // pas de grotte
            assertEquals("0", sc.nextLine().trim());
            // inventaire
            int nInv = Integer.parseInt(sc.nextLine().trim());
            assertEquals(2, nInv);
            String[] s1 = sc.nextLine().split(",");
            assertEquals(100, Integer.parseInt(s1[0]));
            assertEquals(12, Integer.parseInt(s1[1]));
            String[] s2 = sc.nextLine().split(",");
            assertEquals(200, Integer.parseInt(s2[0]));
            assertEquals(35, Integer.parseInt(s2[2]), "la durabilité est conservée");
        }
    }

    @Test
    @DisplayName("La hotbar est sauvegardée sur une ligne, '-' pour les cases vides")
    void save_hotbar_estEcrite() throws Exception {
        player.getHotbar()[0] = new ItemStack(100, 5);
        player.getHotbar()[3] = new ItemStack(200, 1, 17);
        // les autres cases restent null

        SaveManager.saveGameToFile(player, engine, TEST_SAVE);

        try (Scanner sc = new Scanner(new File(TEST_SAVE))) {
            String line;
            // on saute jusqu'à la dernière ligne (la hotbar)
            String lastLine = null;
            while (sc.hasNextLine()) {
                String l = sc.nextLine();
                if (!l.trim().isEmpty()) lastLine = l;
            }
            assertNotNull(lastLine);
            String[] slots = lastLine.split(";", -1);
            assertEquals(9, slots.length, "9 cases attendues dans la hotbar");
            assertTrue(slots[0].startsWith("100,5,"));
            assertEquals("-", slots[1]);
            assertEquals("-", slots[2]);
            assertTrue(slots[3].startsWith("200,1,17"));
            assertEquals("-", slots[8]);
        }
    }

    @Test
    @DisplayName("getSaveScannerForFile renvoie null si le fichier n'existe pas")
    void getSaveScanner_fichierAbsent_renvoieNull() {
        assertNull(SaveManager.getSaveScannerForFile("fichier_inexistant_12345.txt"));
    }
}
