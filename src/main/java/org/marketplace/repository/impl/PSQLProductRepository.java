package org.marketplace.repository.impl;

import org.marketplace.config.DBConfig;
import org.marketplace.exception.DataAccessException;
import org.marketplace.model.Product;
import org.marketplace.repository.ProductRepository;
import org.marketplace.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL implementation of {@link ProductRepository}.
 */
public class PSQLProductRepository implements ProductRepository {

    private final DBConfig db = DBConfig.getInstance();

    // -----------------------------------------------------------------------
    // SQL constants
    // -----------------------------------------------------------------------

    /** Common SELECT with category name and seller username from JOINs. */
    private static final String SELECT_BASE =
            "SELECT p.id, p.seller_id, p.category_id, p.name, p.price, p.stock, p.is_active, " +
            "       c.name AS category_name, u.username AS seller_username " +
            "FROM   products p " +
            "JOIN   categories c ON c.id = p.category_id " +
            "JOIN   users      u ON u.id = p.seller_id ";

    private static final String FIND_ALL_ACTIVE =
            SELECT_BASE + "WHERE p.is_active = TRUE ORDER BY p.name";

    private static final String FIND_ALL =
            SELECT_BASE + "ORDER BY p.id";

    private static final String FIND_BY_SELLER =
            SELECT_BASE + "WHERE p.seller_id = ? ORDER BY p.name";

    private static final String FIND_BY_CATEGORY =
            SELECT_BASE + "WHERE p.category_id = ? AND p.is_active = TRUE ORDER BY p.name";

    private static final String SEARCH_BY_NAME =
            SELECT_BASE + "WHERE p.is_active = TRUE AND LOWER(p.name) LIKE LOWER(?) ORDER BY p.name";

    private static final String FIND_BY_ID =
            SELECT_BASE + "WHERE p.id = ?";

    private static final String INSERT =
            "INSERT INTO products (seller_id, category_id, name, price, stock, is_active) " +
            "VALUES (?, ?, ?, ?, ?, ?) ";

    private static final String UPDATE =
            "UPDATE products SET name = ?, price = ?, stock = ?, category_id = ?, is_active = ? " +
            "WHERE id = ?";

    private static final String DEACTIVATE =
            "UPDATE products SET is_active = FALSE WHERE id = ?";

    private static final String REACTIVATE =
            "UPDATE products SET is_active = TRUE WHERE id = ?";

    private static final String DELETE =
            "DELETE FROM products WHERE id = ?";

    private static final String DECREMENT_STOCK =
            "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";

    private static final String INCREMENT_STOCK =
            "UPDATE products SET stock = stock + ? WHERE id = ?";

    // -----------------------------------------------------------------------
    // Interface implementation
    // -----------------------------------------------------------------------

    @Override
    public List<Product> findAllActive() {
        return query(FIND_ALL_ACTIVE);
    }

    @Override
    public List<Product> findAll() {
        return query(FIND_ALL);
    }

    @Override
    public List<Product> findBySeller(long sellerId) {
        return query(FIND_BY_SELLER, sellerId);
    }

    @Override
    public List<Product> findByCategory(long categoryId) {
        return query(FIND_BY_CATEGORY, categoryId);
    }

    @Override
    public List<Product> searchByName(String keyword) {
        return query(SEARCH_BY_NAME, "%" + keyword + "%");
    }

    @Override
    public Optional<Product> findById(long id) {
        List<Product> results = query(FIND_BY_ID, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Product save(Product product) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepareWithKeys(conn, INSERT,
                     product.getSellerId(),
                     product.getCategoryId(),
                     product.getName(),
                     product.getPrice(),
                     product.getStock(),
                     product.isActive())) {

            stmt.executeUpdate();
            product.setId(JdbcUtil.getGeneratedKey(stmt));
            return product;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert product", e);
        }
    }

    @Override
    public boolean update(Product product) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, UPDATE,
                     product.getName(),
                     product.getPrice(),
                     product.getStock(),
                     product.getCategoryId(),
                     product.isActive(),
                     product.getId())) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update product id=" + product.getId(), e);
        }
    }

    @Override
    public boolean deactivate(long id) {
        return executeUpdate(DEACTIVATE, id);
    }

    @Override
    public boolean reactivate(long id) {
        return executeUpdate(REACTIVATE, id);
    }

    @Override
    public boolean delete(long id) {
        return executeUpdate(DELETE, id);
    }

    @Override
    public boolean decrementStock(long productId, int quantity) {
        // The WHERE clause ensures stock never goes negative
        return executeUpdate(DECREMENT_STOCK, quantity, productId, quantity);
    }

    @Override
    public boolean incrementStock(long productId, int quantity) {
        return executeUpdate(INCREMENT_STOCK, quantity, productId);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private List<Product> query(String sql, Object... params) {
        List<Product> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, sql, params);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Product query failed: " + sql, e);
        }
        return list;
    }

    private boolean executeUpdate(String sql, Object... params) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, sql, params)) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Product update failed: " + sql, e);
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        return Product.builder()
                .id(rs.getLong("id"))
                .sellerId(rs.getLong("seller_id"))
                .categoryId(rs.getLong("category_id"))
                .categoryName(rs.getString("category_name"))
                .sellerUsername(rs.getString("seller_username"))
                .name(rs.getString("name"))
                .price(rs.getBigDecimal("price"))
                .stock(rs.getInt("stock"))
                .active(rs.getBoolean("is_active"))
                .build();
    }
}
