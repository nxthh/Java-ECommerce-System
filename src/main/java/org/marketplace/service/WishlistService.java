package org.marketplace.service;

import lombok.RequiredArgsConstructor;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.*;
import org.marketplace.repository.WishlistRepository;

import java.sql.SQLException;

@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository repository;

    public TableData list(User user) throws SQLException {
        UserService.requireRole(user, "CUSTOMER");
        return repository.findByUser(user.getId());
    }

    public void add(User user, long productId) throws SQLException {
        UserService.requireRole(user, "CUSTOMER");

        if (repository.add(user.getId(), productId) == 0) {
            throw new ValidationException(
                    "Product unavailable or already in wishlist."
            );
        }
    }

    public void remove(User user, long productId) throws SQLException {
        UserService.requireRole(user, "CUSTOMER");
        UserService.changed(repository.remove(user.getId(), productId));
    }
}