package org.marketplace.controller;

import org.marketplace.model.CartItem;
import org.marketplace.service.CartService;

import java.util.List;

public class CartController {
    private final CartService cartService;

    public CartController() {
        this.cartService = new CartService();
    }

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    public List<CartItem> getCart(long userId) {
        return cartService.getCart(userId);
    }

    public CartItem addToCart(long userId, long productId, int quantity) {
        return cartService.addToCart(userId, productId, quantity);
    }

    public boolean updateQuantity(long userId, long productId, int quantity) {
        return cartService.updateQuantity(userId, productId, quantity);
    }

    public boolean removeFromCart(long userId, long productId) {
        return cartService.removeFromCart(userId, productId);
    }

    public void clearCart(long userId) {
        cartService.clearCart(userId);
    }
}
