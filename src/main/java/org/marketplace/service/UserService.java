package org.marketplace.service;

import lombok.RequiredArgsConstructor;
import org.marketplace.exception.AuthorizationException;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.TableData;
import org.marketplace.model.User;
import org.marketplace.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> users(User currentUser) throws SQLException {
        requireRole(currentUser, "ADMIN");

        return userRepository.findAll();
    }

    public TableData profile(User currentUser) throws SQLException {
        requireRole(currentUser, "ADMIN", "SELLER", "CUSTOMER");

        return userRepository.findById(currentUser.getId());
    }

    public void updateName(User currentUser, String fullName)
            throws SQLException {

        requireRole(currentUser, "ADMIN", "SELLER", "CUSTOMER");

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException(
                    "Full name is required."
            );
        }

        fullName = fullName.trim();

        if (fullName.length() > 100) {
            throw new ValidationException(
                    "Full name must contain at most 100 characters."
            );
        }

        int affectedRows = userRepository.updateName(
                currentUser.getId(),
                fullName
        );

        changed(affectedRows);

        currentUser.setFullName(fullName);
    }

    public static void requireRole(
            User currentUser,
            String... allowedRoles
    ) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new AuthorizationException(
                    "Please log in first."
            );
        }

        for (String role : allowedRoles) {
            if (role.equals(currentUser.getRole())) {
                return;
            }
        }

        throw new AuthorizationException(
                "You do not have permission to perform this action."
        );
    }

    public static void changed(int affectedRows) {
        if (affectedRows == 0) {
            throw new ValidationException(
                    "Record not found or action not allowed."
            );
        }
    }
}