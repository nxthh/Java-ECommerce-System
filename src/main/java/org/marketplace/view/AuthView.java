package org.marketplace.view;

import org.marketplace.controller.AuthController;
import org.marketplace.exception.AuthorizationException;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.User;
import org.marketplace.util.InputUtil;

/**
 * Console view for user login and registration.
 */
public class AuthView {

    private final AuthController authController;

    public AuthView() {
        this.authController = new AuthController();
    }

    public AuthView(AuthController authController) {
        this.authController = authController;
    }

    public User showAuthMenu() {
        while (true) {
            ConsoleView.printHeader("Welcome to E-Commerce Marketplace");
            System.out.println("  1. Login");
            System.out.println("  2. Register (Customer / Seller)");
            System.out.println("  0. Exit");

            int choice = InputUtil.readChoice("\nEnter choice: ", 0, 2);
            System.out.println();

            switch (choice) {
                case 1 -> {
                    User user = login();
                    if (user != null) return user;
                }
                case 2 -> {
                    User user = register();
                    if (user != null) return user;
                }
                case 0 -> {
                    System.out.println("Goodbye!");
                    return null;
                }
            }
        }
    }

    public User login() {
        ConsoleView.printHeader("User Login");
        String username = InputUtil.readLine("  Username: ");
        String password = InputUtil.readLine("  Password: ");

        try {
            User user = authController.login(username, password);
            ConsoleView.printSuccess("Welcome back, " + user.getFullName() + " (" + user.getRole() + ")!");
            return user;
        } catch (ValidationException | AuthorizationException e) {
            ConsoleView.printError(e.getMessage());
            return null;
        }
    }

    public User register() {
        ConsoleView.printHeader("User Registration");
        String username = InputUtil.readLine("  Username : ");
        String password = InputUtil.readLine("  Password : ");
        String fullName = InputUtil.readLine("  Full Name: ");

        System.out.println("  Select Role:");
        System.out.println("    1. CUSTOMER");
        System.out.println("    2. SELLER");
        int roleChoice = InputUtil.readChoice("  Role: ", 1, 2);
        User.Role role = roleChoice == 2 ? User.Role.SELLER : User.Role.CUSTOMER;

        try {
            User user = authController.register(username, password, fullName, role);
            ConsoleView.printSuccess("Account created successfully! Logged in as " + user.getUsername());
            authController.login(username, password);
            return user;
        } catch (ValidationException e) {
            ConsoleView.printError(e.getMessage());
            return null;
        }
    }
}
