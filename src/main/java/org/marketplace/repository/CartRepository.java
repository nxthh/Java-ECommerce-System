package org.marketplace.repository;

import org.marketplace.model.CartItem;

import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for shopping cart items.
 * Implemented by Sreymey.
 */
public interface CartRepository {

    /** Returns all cart items for the given user, enriched with product details. */
    List<CartItem> findByUser(long userId);

    /** Looks up a single cart item by (userId, productId). */
    Optional<CartItem> findByUserAndProduct(long userId, long productId);

    /**
     * Adds a product to the cart, or increments quantity if already present.
     *
     * @return the updated/created {@link CartItem}.
     */
    CartItem addOrUpdate(long userId, long productId, int quantity);

    /**
     * Sets the quantity of a cart item.
     *
     * @return {@code true} if the row was updated.
     */
    boolean updateQuantity(long userId, long productId, int quantity);

    /**
     * Removes a single item from the cart.
     *
     * @return {@code true} if a row was deleted.
     */
    boolean remove(long userId, long productId);

    /** Removes all items from the given user's cart. */
    void clearCart(long userId);
}
