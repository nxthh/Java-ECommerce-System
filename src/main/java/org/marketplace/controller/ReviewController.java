package org.marketplace.controller;

import lombok.RequiredArgsConstructor;
import org.marketplace.model.User;
import org.marketplace.service.ReviewService;
import org.marketplace.util.InputUtil;

import java.sql.SQLException;

@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    public void list() throws SQLException {
        InputUtil.table(
                service.list(InputUtil.id("Product ID: "))
        );
    }

    public void save(User user) throws SQLException {
        long productId = InputUtil.id("Product ID: ");
        int rating;

        do {
            rating = InputUtil.number("Rating (1–5): ");

            if (rating < 1 || rating > 5) {
                InputUtil.message("Rating must be 1–5.");
            }
        } while (rating < 1 || rating > 5);

        service.save(
                user, productId, rating,
                InputUtil.text("Comment: ")
        );

        InputUtil.message("Review saved.");
    }
}