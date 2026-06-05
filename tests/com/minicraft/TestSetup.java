package com.minicraft;

import javafx.application.Platform;
import java.util.concurrent.CountDownLatch;

// Petite classe utilitaire pour initialiser JavaFX une seule fois avant les tests.
// Plusieurs classes du projet (Player, ItemDefinition, HUD, ...) chargent des Images
// au moment du new(), ce qui nécessite que le toolkit JavaFX soit démarré.
public final class TestSetup {
    private static boolean started = false;

    public static synchronized void ensureFxStarted() {
        if (started) return;
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await();
        } catch (IllegalStateException alreadyStarted) {
            // JavaFX est déjà initialisé (cas où plusieurs classes de test tournent dans le même process)
        } catch (Exception e) {
            throw new RuntimeException("Impossible de démarrer JavaFX pour les tests", e);
        }
        started = true;
    }

    private TestSetup() {}
}
