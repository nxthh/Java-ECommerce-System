package org.marketplace.controller;

import lombok.RequiredArgsConstructor;
import org.marketplace.model.User;
import org.marketplace.service.WishlistService;
import org.marketplace.util.InputUtil;

import java.sql.SQLException;

@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService service;

    public void list(User user) throws SQLException {
        InputUtil.table(service.list(user));
    }

    public void add(User user) throws SQLException {
        service.add(user, InputUtil.id("Product ID: "));
        InputUtil.message("Added to wishlist.");
    }

    public void remove(User user) throws SQLException {
        list(user);
        service.remove(user, InputUtil.id("Product ID: "));
        InputUtil.message("Removed from wishlist.");
    }
}