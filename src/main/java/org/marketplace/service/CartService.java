package org.marketplace.service;

import lombok.RequiredArgsConstructor;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.*;
import org.marketplace.repository.*;
import org.marketplace.util.JdbcUtil;

import java.sql.*;

@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public TableData list(User user) throws SQLException {
        UserService.requireRole(user, "CUSTOMER");
        return cartRepository.findByUser(user.getId());
    }

    public void set(User user, long productId, int quantity)
            throws SQLException {

        UserService.requireRole(user, "CUSTOMER");

        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive.");
        }

        try (Connection connection = JdbcUtil.open()) {
            connection.setAutoCommit(false);

            try {
                userRepository.lock(connection, user.getId());

                Product product = productRepository.findById(
                        connection, productId, true
                );

                if (product == null  !product.isActive()
                product.getStock() < quantity) {
                    throw new ValidationException(
                            "Product unavailable or insufficient stock."
                    );
                }

                cartRepository.setQuantity(
                        connection, user.getId(), productId, quantity
                );

                connection.commit();

            } catch (SQLException | RuntimeException exception) {
                JdbcUtil.rollback(connection, exception);
                throw exception;
            }
        }
    }

    public void remove(User user, long productId) throws SQLException {
        UserService.requireRole(user, "CUSTOMER");

        try (Connection connection = JdbcUtil.open()) {
            connection.setAutoCommit(false);

            try {
                userRepository.lock(connection, user.getId());

                UserService.changed(cartRepository.remove(
                        connection, user.getId(), productId
                ));

                connection.commit();

            } catch (SQLException | RuntimeException exception) {
                JdbcUtil.rollback(connection, exception);
                throw exception;
            }
        }
    }
}