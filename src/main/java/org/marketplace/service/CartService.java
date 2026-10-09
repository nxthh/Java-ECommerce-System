package org.marketplace.service;

import org.marketplace.model.CartItem;
import org.marketplace.repository.CartRepository;
import org.marketplace.repository.ProductRepository;
import org.marketplace.repository.impl.PSQLCartRepository;
import org.marketplace.repository.impl.PSQLProductRepository;

import java.util.List;

public class CartService {
    private final CartRepository cartRepo;
    private final ProductRepository productRepo;

    public CartService() {
        this(new PSQLCartRepository(), new PSQLProductRepository());
    }

    public CartService(CartRepository cartRepo, ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    public List<CartItem> getCart(long userId) {
        return cartRepo.findByUser(userId);
    }

    public CartItem addToCart(long userId, long productId, int quantity) {
        return cartRepo.addOrUpdate(userId, productId, quantity);
    }

    public boolean updateQuantity(long userId, long productId, int quantity) {
        return cartRepo.updateQuantity(userId, productId, quantity);
    }

    public boolean removeFromCart(long userId, long productId) {
        return cartRepo.remove(userId, productId);
    }

    public void clearCart(long userId) {
        cartRepo.clearCart(userId);
    }
}
