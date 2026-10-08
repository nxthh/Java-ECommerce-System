package org.marketplace.fx;

import org.marketplace.Main;

/**
 * Main launcher entry point configured in build.gradle.
 */
public class FxLauncher {
    public static void main(String[] args) {
        // If GUI is requested or supported, launch JavaFX; otherwise fallback to console
        try {
            MarketplaceApp.main(args);
        } catch (Throwable t) {
            System.out.println("GUI initialization failed or headless mode detected. Starting console mode...");
            Main.main(args);
        }
    }
}
