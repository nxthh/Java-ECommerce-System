package org.marketplace.controller;

import org.marketplace.model.User;
import org.marketplace.service.AuthService;

/**
 * Controller mediating authentication actions.
 */
public class AuthController {

    private final AuthService authService;

    public AuthController() {
        this.authService = new AuthService();
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public User register(String username, String password, String fullName, User.Role role) {
        return authService.register(username, password, fullName, role);
    }

    public User login(String username, String password) {
        return authService.login(username, password);
    }

    public void logout() {
        authService.logout();
    }

    public User getCurrentUser() {
        return authService.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return authService.isLoggedIn();
    }
}
