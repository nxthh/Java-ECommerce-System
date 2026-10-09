package org.marketplace.service;

import org.marketplace.model.Product;
import org.marketplace.repository.WishlistRepository;
import org.marketplace.repository.impl.PSQLWishlistRepository;

import java.util.List;

public class WishlistService {
    private final WishlistRepository wishlistRepo;

    public WishlistService() {
        this(new PSQLWishlistRepository());
    }

    public WishlistService(WishlistRepository wishlistRepo) {
        this.wishlistRepo = wishlistRepo;
    }

    public List<Product> getWishlist(long userId) {
        return wishlistRepo.findByUser(userId);
    }

    public boolean addToWishlist(long userId, long productId) {
        return wishlistRepo.add(userId, productId);
    }

    public boolean removeFromWishlist(long userId, long productId) {
        return wishlistRepo.remove(userId, productId);
    }

    public boolean isInWishlist(long userId, long productId) {
        return wishlistRepo.exists(userId, productId);
    }
}
