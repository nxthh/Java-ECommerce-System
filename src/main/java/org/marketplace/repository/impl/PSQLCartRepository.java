package org.marketplace.repository.impl;

import org.marketplace.config.DBConfig;
import org.marketplace.exception.DataAccessException;
import org.marketplace.model.CartItem;
import org.marketplace.repository.CartRepository;
import org.marketplace.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL implementation of {@link CartRepository}.
 * To be fleshed out by Sreymey.
 */
public class PSQLCartRepository implements CartRepository {

    private final DBConfig db = DBConfig.getInstance();

    private static final String SELECT_BASE =
            "SELECT ci.user_id, ci.product_id, ci.quantity, " +
            "       p.name AS product_name, p.price AS unit_price, p.stock " +
            "FROM   cart_items ci " +
            "JOIN   products p ON p.id = ci.product_id ";

    private static final String FIND_BY_USER     = SELECT_BASE + "WHERE ci.user_id = ?";
    private static final String FIND_BY_USER_AND_PRODUCT =
            SELECT_BASE + "WHERE ci.user_id = ? AND ci.product_id = ?";

    private static final String UPSERT =
            "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?) " +
            "ON CONFLICT (user_id, product_id) DO UPDATE SET quantity = cart_items.quantity + EXCLUDED.quantity";

    private static final String UPDATE_QTY =
            "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?";

    private static final String REMOVE =
            "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";

    private static final String CLEAR =
            "DELETE FROM cart_items WHERE user_id = ?";

    @Override
    public List<CartItem> findByUser(long userId) {
        List<CartItem> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, FIND_BY_USER, userId);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch cart for user=" + userId, e);
        }
        return list;
    }

    @Override
    public Optional<CartItem> findByUserAndProduct(long userId, long productId) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, FIND_BY_USER_AND_PRODUCT, userId, productId);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find cart item", e);
        }
    }

    @Override
    public CartItem addOrUpdate(long userId, long productId, int quantity) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, UPSERT, userId, productId, quantity)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to upsert cart item", e);
        }
        return findByUserAndProduct(userId, productId).orElseThrow();
    }

    @Override
    public boolean updateQuantity(long userId, long productId, int quantity) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, UPDATE_QTY, quantity, userId, productId)) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update cart item quantity", e);
        }
    }

    @Override
    public boolean remove(long userId, long productId) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, REMOVE, userId, productId)) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to remove cart item", e);
        }
    }

    @Override
    public void clearCart(long userId) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, CLEAR, userId)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear cart for user=" + userId, e);
        }
    }

    private CartItem map(ResultSet rs) throws SQLException {
        return CartItem.builder()
                .userId(rs.getLong("user_id"))
                .productId(rs.getLong("product_id"))
                .productName(rs.getString("product_name"))
                .unitPrice(rs.getBigDecimal("unit_price"))
                .stock(rs.getInt("stock"))
                .quantity(rs.getInt("quantity"))
                .build();
    }
}
