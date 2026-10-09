package org.marketplace.repository;

import org.marketplace.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for {@link Product} entities.
 */
public interface ProductRepository {

    /**
     * Returns all active products (visible to customers and sellers),
     * joined with their category name and seller username.
     */
    List<Product> findAllActive();

    /**
     * Returns every product regardless of {@code is_active} status.
     * Intended for admin views.
     */
    List<Product> findAll();

    /**
     * Returns all products owned by the given seller (active and inactive).
     *
     * @param sellerId the seller's user id.
     */
    List<Product> findBySeller(long sellerId);

    /**
     * Returns all active products belonging to the given category.
     *
     * @param categoryId the category id.
     */
    List<Product> findByCategory(long categoryId);

    /**
     * Full-text search by product name (case-insensitive, partial match)
     * on active products.
     *
     * @param keyword search keyword.
     */
    List<Product> searchByName(String keyword);

    /**
     * Looks up a single product by its primary key.
     * Returns the product regardless of active status.
     */
    Optional<Product> findById(long id);

    /**
     * Inserts a new product record.
     *
     * @return the saved product with its generated {@code id}.
     */
    Product save(Product product);

    /**
     * Updates name, price, stock, category and active status of an existing product.
     *
     * @return {@code true} if a row was updated.
     */
    boolean update(Product product);

    /**
     * Soft-deletes a product by setting {@code is_active = false}.
     *
     * @return {@code true} if a row was updated.
     */
    boolean deactivate(long id);

    /**
     * Re-activates a soft-deleted product.
     *
     * @return {@code true} if a row was updated.
     */
    boolean reactivate(long id);

    /**
     * Hard-deletes a product from the database.
     * Use only when the product has no order history.
     *
     * @return {@code true} if a row was deleted.
     */
    boolean delete(long id);

    /**
     * Decrements the stock of a product by {@code quantity}.
     * Used when an order is placed.
     *
     * @return {@code true} if the update succeeded.
     */
    boolean decrementStock(long productId, int quantity);

    /**
     * Increments the stock of a product by {@code quantity}.
     * Used when an order is cancelled.
     *
     * @return {@code true} if the update succeeded.
     */
    boolean incrementStock(long productId, int quantity);
}
