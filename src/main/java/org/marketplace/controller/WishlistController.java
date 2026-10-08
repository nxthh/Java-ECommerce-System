package org.marketplace.controller;

import org.marketplace.model.Product;
import org.marketplace.service.WishlistService;

import java.util.List;

public class WishlistController {
    private final WishlistService wishlistService;

    public WishlistController() {
        this.wishlistService = new WishlistService();
    }

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    public List<Product> getWishlist(long userId) {
        return wishlistService.getWishlist(userId);
    }

    public boolean addToWishlist(long userId, long productId) {
        return wishlistService.addToWishlist(userId, productId);
    }

    public boolean removeFromWishlist(long userId, long productId) {
        return wishlistService.removeFromWishlist(userId, productId);
    }

    public boolean isInWishlist(long userId, long productId) {
        return wishlistService.isInWishlist(userId, productId);
    }
}
