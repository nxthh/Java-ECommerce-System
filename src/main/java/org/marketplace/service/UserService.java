package org.marketplace.service;

import org.marketplace.exception.ValidationException;
import org.marketplace.model.User;
import org.marketplace.repository.UserRepository;
import org.marketplace.repository.impl.PSQLUserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing user profiles and lookup.
 */
public class UserService {

    private final UserRepository userRepo;

    public UserService() {
        this(new PSQLUserRepository());
    }

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public Optional<User> getUserById(long id) {
        return userRepo.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public List<User> getUsersByRole(User.Role role) {
        return userRepo.findByRole(role);
    }

    public boolean updateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new ValidationException("User and User ID must not be null");
        }
        return userRepo.update(user);
    }

    public boolean deleteUser(long id) {
        return userRepo.delete(id);
    }
}
