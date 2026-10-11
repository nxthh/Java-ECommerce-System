package org.marketplace.service;

import lombok.RequiredArgsConstructor;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.*;
import org.marketplace.repository.ReviewRepository;

import java.sql.SQLException;

@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository repository;

    public TableData list(long productId) throws SQLException {
        return repository.findByProduct(productId);
    }

    public void save(
            User user, long productId, int rating, String comment
    ) throws SQLException {

        UserService.requireRole(user, "CUSTOMER");

        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be 1–5.");
        }

        if (repository.save(
                user.getId(), productId, rating, comment) == 0) {
            throw new ValidationException(
                    "Purchase this product before reviewing it."
            );
        }
    }
}