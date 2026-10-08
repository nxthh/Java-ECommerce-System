package org.marketplace.controller;

import org.marketplace.model.User;
import org.marketplace.service.UserService;

import java.util.List;
import java.util.Optional;

/**
 * Controller mediating user operations.
 */
public class UserController {

    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public List<User> listAllUsers() {
        return userService.getAllUsers();
    }

    public Optional<User> getUserById(long id) {
        return userService.getUserById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userService.getUserByUsername(username);
    }

    public List<User> getUsersByRole(User.Role role) {
        return userService.getUsersByRole(role);
    }

    public boolean updateUser(User user) {
        return userService.updateUser(user);
    }

    public boolean deleteUser(long id) {
        return userService.deleteUser(id);
    }
}
