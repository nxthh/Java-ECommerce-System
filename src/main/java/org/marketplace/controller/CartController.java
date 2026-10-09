package org.marketplace.controller;

import lombok.RequiredArgsConstructor;
import org.marketplace.model.User;
import org.marketplace.service.CartService;
import org.marketplace.util.InputUtil;

import java.sql.SQLException;

@RequiredArgsConstructor
public class CartController {

    private final CartService service;

    public void list(User user) throws SQLException {
        InputUtil.table(service.list(user));
    }

    public void set(User user) throws SQLException {
        long productId = InputUtil.id("Product ID: ");
        int quantity;

        do {
            quantity = InputUtil.number("New cart quantity: ");

            if (quantity <= 0) {
                InputUtil.message("Quantity must be positive.");
            }
        } while (quantity <= 0);

        service.set(user, productId, quantity);
        InputUtil.message("Cart saved.");
    }

    public void remove(User user) throws SQLException {
        list(user);

        service.remove(user, InputUtil.id("Product ID to remove: "));
        InputUtil.message("Removed from cart.");
    }
}
