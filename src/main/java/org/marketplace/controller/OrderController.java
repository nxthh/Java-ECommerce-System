package org.marketplace.controller;

import lombok.RequiredArgsConstructor;
import org.marketplace.model.*;
import org.marketplace.service.*;
import org.marketplace.util.InputUtil;

import java.math.BigDecimal;
import java.sql.SQLException;

@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;
    private final CartService cartService;

    public void history(User user) throws SQLException {
        InputUtil.table(service.history(user));
    }

    public void details(User user) throws SQLException {
        TableData data = service.details(
                user, InputUtil.id("Order ID: ")
        );

        if (data.getRow().isEmpty()) {
            InputUtil.message("Order not found or access denied.");
        } else {
            InputUtil.table(data);
        }
    }

    public void complete(User user) throws SQLException {
        long id = InputUtil.id("Order ID: ");

        if (InputUtil.yes("Mark this paid order completed?")) {
            service.complete(user, id);
            InputUtil.message("Order completed.");
        }
    }

    public void checkout(User user) throws SQLException {
        TableData cart = cartService.list(user);
        InputUtil.table(cart);

        if (cart.getRow().isEmpty()) {
            return;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (String[] row : cart.getRow()) {
            total = total.add(new BigDecimal(row[4]));
        }

        InputUtil.message("Total: " + total);

        int choice = InputUtil.menu(
                "PAYMENT METHOD", "Cancel",
                "Cash", "Card", "QR"
        );

        if (choice == 0) {
            return;
        }

        String method;

        if (choice == 1) {
            method = "CASH";
        } else if (choice == 2) {
            method = "CARD";
        } else {
            method = "QR";
        }

        boolean success = InputUtil.yes("Simulate successful payment?");

        if (!InputUtil.yes("Confirm checkout?")) {
            return;
        }

        long id = service.checkout(user, method, success, total);

        if (success) {
            InputUtil.message("Payment successful. Order ID: " + id);
        } else {
            InputUtil.message(
                    "Payment failed. Cancelled order ID: " + id
                            + ". Cart and stock are unchanged."
            );
        }
    }
}