package org.marketplace.service;

import lombok.RequiredArgsConstructor;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.User;
import org.marketplace.repository.UserRepository;
import org.marketplace.util.PasswordUtil;

import java.sql.SQLException;

@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;

    public void register(
            String username, String password,
            String fullName, String role
    ) throws SQLException {

        username = username.trim();
        fullName = fullName.trim();

        if (!username.matches("[A-Za-z0-9_]{3,50}")) {
            throw new ValidationException(
                    "Username: 3–50 letters, digits or underscores."
            );
        }

        if (password.length() < 8) {
            throw new ValidationException(
                    "Password must contain at least 8 characters."
            );
        }

        if (fullName.isEmpty() || fullName.length() > 100) {
            throw new ValidationException("Invalid full name.");
        }

        if (!"CUSTOMER".equals(role) && !"SELLER".equals(role)) {
            throw new ValidationException("Invalid registration role.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setFullName(fullName);
        user.setRole(role);

        repository.save(user);
    }

    public User login(String username, String password)
            throws SQLException {

        User user = repository.findByUsername(username.trim());

        if (user == null
                || !PasswordUtil.matches(password, user.getPasswordHash())) {
            throw new ValidationException(
                    "Invalid username or password."
            );
        }

        user.setPasswordHash(null);
        return user;
    }
}