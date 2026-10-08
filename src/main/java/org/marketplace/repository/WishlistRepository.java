package org.marketplace.repository;

import org.marketplace.model.Product;

import java.util.List;

/**
 * Data-access contract for wishlist items.
 * Implemented by Sreymey.
 */
public interface WishlistRepository {

    /** Returns all products wishlisted by the given user. */
    List<Product> findByUser(long userId);

    /**
     * Adds a product to the user's wishlist (idempotent).
     *
     * @return {@code true} if a new row was inserted.
     */
    boolean add(long userId, long productId);

    /**
     * Removes a product from the user's wishlist.
     *
     * @return {@code true} if a row was deleted.
     */
    boolean remove(long userId, long productId);

    /** Returns {@code true} if the product is in the user's wishlist. */
    boolean exists(long userId, long productId);
}
