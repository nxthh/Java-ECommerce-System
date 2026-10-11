package org.marketplace.service;

import lombok.RequiredArgsConstructor;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.*;
import org.marketplace.repository.ProductRepository;
import org.marketplace.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.*;

@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    public TableData search(String keyword, long categoryId)
            throws SQLException {

        if (categoryId < 0) {
            throw new ValidationException("Invalid category ID.");
        }

        return repository.search(keyword.trim(), categoryId, 0);
    }

    public TableData own(User user) throws SQLException {
        UserService.requireRole(user, "SELLER");
        return repository.search("", 0, user.getId());
    }

    public void validate(Product product) {
        if (product.getCategoryId() == null
                || product.getCategoryId() <= 0) {
            throw new ValidationException("Invalid category ID.");
        }

        if (product.getName() == null
                || product.getName().isBlank()
                || product.getName().trim().length() > 150) {
            throw new ValidationException(
                    "Product name must contain 1–150 characters."
            );
        }

        product.setName(product.getName().trim());

        BigDecimal price = product.getPrice();

        if (price == null || price.signum() <= 0
                || price.stripTrailingZeros().scale() > 2
                || price.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new ValidationException(
                    "Invalid price. Use a positive amount with up to 2 decimals."
            );
        }

        if (product.getStock() < 0) {
            throw new ValidationException("Stock cannot be negative.");
        }
    }

    public void save(User user, Product product) throws SQLException {
        UserService.requireRole(user, "SELLER");
        validate(product);
        product.setSellerId(user.getId());

        if (product.getId() == null) {
            try (Connection connection = JdbcUtil.open()) {
                repository.save(connection, product);
            }
        } else {
            UserService.changed(repository.update(product));
        }
    }

    public void active(User user, long id, boolean active)
            throws SQLException {

        UserService.requireRole(user, "SELLER");

        UserService.changed(
                repository.setActive(id, user.getId(), active)
        );
    }
}