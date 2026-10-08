package org.marketplace.service;

import org.marketplace.exception.AuthorizationException;
import org.marketplace.exception.ValidationException;
import org.marketplace.model.Product;
import org.marketplace.model.User;
import org.marketplace.repository.CategoryRepository;
import org.marketplace.repository.ProductRepository;
import org.marketplace.repository.impl.PSQLCategoryRepository;
import org.marketplace.repository.impl.PSQLProductRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business-logic layer for product management.
 *
 * <ul>
 *   <li>Sellers can create, update, deactivate, and reactivate their own products.</li>
 *   <li>Admins can update, deactivate, reactivate, or hard-delete any product.</li>
 *   <li>Customers and guests can search / browse active products.</li>
 * </ul>
 */
public class ProductService {

    private final ProductRepository  productRepo;
    private final CategoryRepository categoryRepo;

    /** Default constructor wires PostgreSQL implementations. */
    public ProductService() {
        this(new PSQLProductRepository(), new PSQLCategoryRepository());
    }

    /** Injection constructor for testing. */
    public ProductService(ProductRepository productRepo, CategoryRepository categoryRepo) {
        this.productRepo  = productRepo;
        this.categoryRepo = categoryRepo;
    }

    // -----------------------------------------------------------------------
    // Browse / search (all roles)
    // -----------------------------------------------------------------------

    /** Returns all active products. */
    public List<Product> getAllActiveProducts() {
        return productRepo.findAllActive();
    }

    /** Returns all active products in a category. */
    public List<Product> getProductsByCategory(long categoryId) {
        return productRepo.findByCategory(categoryId);
    }

    /**
     * Returns active products whose name contains the keyword
     * (case-insensitive partial match).
     */
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) return productRepo.findAllActive();
        return productRepo.searchByName(keyword.trim());
    }

    /**
     * Returns the product with the given id.
     *
     * @throws ValidationException if not found.
     */
    public Product getProductById(long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ValidationException("Product not found: id=" + id));
    }

    // -----------------------------------------------------------------------
    // Seller operations
    // -----------------------------------------------------------------------

    /** Returns all products (active and inactive) owned by the given seller. */
    public List<Product> getSellerProducts(long sellerId) {
        return productRepo.findBySeller(sellerId);
    }

    /**
     * Creates a new product on behalf of a seller.
     *
     * @param actor the logged-in user (must be SELLER or ADMIN).
     * @throws AuthorizationException if the actor is not a SELLER or ADMIN.
     * @throws ValidationException    if any field is invalid.
     */
    public Product createProduct(User actor, String name, BigDecimal price,
                                 int stock, long categoryId) {
        requireSellerOrAdmin(actor);
        validateProductFields(name, price, stock, categoryId);

        Product product = Product.builder()
                .sellerId(actor.getId())
                .categoryId(categoryId)
                .name(name.trim())
                .price(price)
                .stock(stock)
                .active(true)
                .build();

        return productRepo.save(product);
    }

    /**
     * Updates name, price, stock, and category of a product.
     *
     * <p>A SELLER may only update their own products. An ADMIN may update any product.</p>
     *
     * @throws AuthorizationException if the seller tries to edit another seller's product.
     * @throws ValidationException    if any field is invalid or the product is not found.
     */
    public void updateProduct(User actor, long productId, String name,
                              BigDecimal price, int stock, long categoryId) {
        requireSellerOrAdmin(actor);
        Product existing = getProductById(productId);
        requireOwnerOrAdmin(actor, existing);
        validateProductFields(name, price, stock, categoryId);

        existing.setName(name.trim());
        existing.setPrice(price);
        existing.setStock(stock);
        existing.setCategoryId(categoryId);

        productRepo.update(existing);
    }

    /**
     * Soft-deletes (deactivates) a product so it no longer appears in listings.
     *
     * @throws AuthorizationException if the seller tries to deactivate another seller's product.
     */
    public void deactivateProduct(User actor, long productId) {
        requireSellerOrAdmin(actor);
        Product existing = getProductById(productId);
        requireOwnerOrAdmin(actor, existing);
        productRepo.deactivate(productId);
    }

    /**
     * Re-activates a previously deactivated product.
     *
     * @throws AuthorizationException if the seller tries to reactivate another seller's product.
     */
    public void reactivateProduct(User actor, long productId) {
        requireSellerOrAdmin(actor);
        Product existing = getProductById(productId);
        requireOwnerOrAdmin(actor, existing);
        productRepo.reactivate(productId);
    }

    // -----------------------------------------------------------------------
    // Admin-only operations
    // -----------------------------------------------------------------------

    /** Returns every product (active and inactive). ADMIN only. */
    public List<Product> getAllProducts(User actor) {
        requireAdmin(actor);
        return productRepo.findAll();
    }

    /**
     * Hard-deletes a product. ADMIN only.
     * Will fail at the DB level if there are referencing order_items.
     */
    public void deleteProduct(User actor, long productId) {
        requireAdmin(actor);
        getProductById(productId); // ensure it exists
        productRepo.delete(productId);
    }

    // -----------------------------------------------------------------------
    // Stock helpers (called internally by CartService / OrderService)
    // -----------------------------------------------------------------------

    /**
     * Decrements stock by {@code quantity}.
     *
     * @throws ValidationException if stock is insufficient.
     */
    public void decrementStock(long productId, int quantity) {
        boolean ok = productRepo.decrementStock(productId, quantity);
        if (!ok) {
            throw new ValidationException(
                    "Insufficient stock for product id=" + productId);
        }
    }

    /** Increments stock by {@code quantity} (e.g. on order cancellation). */
    public void incrementStock(long productId, int quantity) {
        productRepo.incrementStock(productId, quantity);
    }

    // -----------------------------------------------------------------------
    // Private validation helpers
    // -----------------------------------------------------------------------

    private void requireSellerOrAdmin(User actor) {
        if (actor.getRole() != User.Role.SELLER && actor.getRole() != User.Role.ADMIN) {
            throw new AuthorizationException("Only sellers and admins can manage products.");
        }
    }

    private void requireAdmin(User actor) {
        if (actor.getRole() != User.Role.ADMIN) {
            throw new AuthorizationException("Only admins can perform this action.");
        }
    }

    private void requireOwnerOrAdmin(User actor, Product product) {
        if (actor.getRole() == User.Role.ADMIN) return;
        if (!product.getSellerId().equals(actor.getId())) {
            throw new AuthorizationException("You can only manage your own products.");
        }
    }

    private void validateProductFields(String name, BigDecimal price, int stock, long categoryId) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Product name must not be blank.");
        }
        if (name.trim().length() > 150) {
            throw new ValidationException("Product name must not exceed 150 characters.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than 0.");
        }
        if (stock < 0) {
            throw new ValidationException("Stock must be 0 or greater.");
        }
        if (categoryRepo.findById(categoryId).isEmpty()) {
            throw new ValidationException("Category not found: id=" + categoryId);
        }
    }
}
