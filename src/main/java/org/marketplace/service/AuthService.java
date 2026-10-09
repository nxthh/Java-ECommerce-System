package org.marketplace.service;

import org.marketplace.exception.AuthorizationException;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.User;
import org.marketplace.repository.UserRepository;
import org.marketplace.repository.impl.PSQLUserRepository;
import org.marketplace.util.PasswordUtil;

import java.util.Optional;

/**
 * Authentication and session service.
 */
public class AuthService {

    private final UserRepository userRepo;
    private User currentUser;

    public AuthService() {
        this(new PSQLUserRepository());
    }

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User register(String username, String rawPassword, String fullName, User.Role role) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }
        if (rawPassword == null || rawPassword.length() < 4) {
            throw new ValidationException("Password must be at least 4 characters long");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Full name cannot be empty");
        }
        if (role == null) {
            role = User.Role.CUSTOMER;
        }

        Optional<User> existing = userRepo.findByUsername(username.trim());
        if (existing.isPresent()) {
            throw new ValidationException("Username '" + username + "' is already taken");
        }

        String passwordHash = PasswordUtil.hash(rawPassword);
        User user = User.builder()
                .username(username.trim())
                .passwordHash(passwordHash)
                .fullName(fullName.trim())
                .role(role)
                .build();

        return userRepo.save(user);
    }

    public User login(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            throw new ValidationException("Username and password required");
        }

        User user = userRepo.findByUsername(username.trim())
                .orElseThrow(() -> new AuthorizationException("Invalid username or password"));

        if (!PasswordUtil.verify(rawPassword, user.getPasswordHash())) {
            throw new AuthorizationException("Invalid username or password");
        }

        this.currentUser = user;
        return user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
