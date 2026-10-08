package org.marketplace.repository.impl;

import org.marketplace.config.DBConfig;
import org.marketplace.exception.DataAccessException;
import org.marketplace.model.Product;
import org.marketplace.repository.WishlistRepository;
import org.marketplace.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL implementation of {@link WishlistRepository}.
 * To be fleshed out by Sreymey.
 */
public class PSQLWishlistRepository implements WishlistRepository {

    private final DBConfig db = DBConfig.getInstance();

    private static final String FIND_BY_USER =
            "SELECT p.id, p.seller_id, p.category_id, p.name, p.price, p.stock, p.is_active, " +
            "       c.name AS category_name, u.username AS seller_username " +
            "FROM   wishlist_items w " +
            "JOIN   products   p ON p.id = w.product_id " +
            "JOIN   categories c ON c.id = p.category_id " +
            "JOIN   users      u ON u.id = p.seller_id " +
            "WHERE  w.user_id = ? " +
            "ORDER BY p.name";

    private static final String ADD    = "INSERT INTO wishlist_items (user_id, product_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
    private static final String REMOVE = "DELETE FROM wishlist_items WHERE user_id = ? AND product_id = ?";
    private static final String EXISTS = "SELECT 1 FROM wishlist_items WHERE user_id = ? AND product_id = ?";

    @Override
    public List<Product> findByUser(long userId) {
        List<Product> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, FIND_BY_USER, userId);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapProduct(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch wishlist for user=" + userId, e);
        }
        return list;
    }

    @Override
    public boolean add(long userId, long productId) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, ADD, userId, productId)) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to add wishlist item", e);
        }
    }

    @Override
    public boolean remove(long userId, long productId) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, REMOVE, userId, productId)) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to remove wishlist item", e);
        }
    }

    @Override
    public boolean exists(long userId, long productId) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, EXISTS, userId, productId);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check wishlist item", e);
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
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
