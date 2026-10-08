package org.marketplace;

import org.marketplace.view.MainMenu;

/**
 * Console entry point for the E-Commerce Marketplace application.
 */
public class Main {
    public static void main(String[] args) {
        MainMenu menu = new MainMenu();
        menu.start();
    }
}
