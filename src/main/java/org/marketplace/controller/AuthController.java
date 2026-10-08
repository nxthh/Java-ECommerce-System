package org.marketplace.controller;

import lombok.RequiredArgsConstructor;
import org.marketplace.model.User;
import org.marketplace.service.AuthService;
import org.marketplace.view.AuthView;

import java.sql.SQLException;

@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;
    private final AuthView view;

    public void register() throws SQLException {
        String username = view.username();
        String password = view.registrationPassword();
        String name = view.fullName();
        String role = view.role();

        if (role == null) {
            return;
        }

        service.register(username, password, name, role);
        view.message("Registration successful. Please log in.");
    }

    public User login() throws SQLException {
        User user = service.login(
                view.loginUsername(),
                view.loginPassword()
        );

        view.message("Welcome, " + user.getFullName());
        return user;
    }
}