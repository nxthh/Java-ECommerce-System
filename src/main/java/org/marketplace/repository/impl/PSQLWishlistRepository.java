package org.marketplace.repository.impl;

import org.marketplace.model.TableData;
import org.marketplace.repository.WishlistRepository;
import org.marketplace.util.JdbcUtil;

import java.sql.SQLException;

public class PSQLWishlistRepository implements WishlistRepository {

    @Override
    public TableData findByUser(long userId) throws SQLException {
        return JdbcUtil.query("""
                SELECT p.id, p.name, p.price, p.stock, p.is_active
                FROM wishlist_items w
                JOIN products p ON p.id = w.product_id
                WHERE w.user_id = ?
                ORDER BY p.id
                """, userId);
    }

    @Override
    public int add(long userId, long productId) throws SQLException {
        return JdbcUtil.update("""
                INSERT INTO wishlist_items(user_id, product_id)
                SELECT ?, id FROM products
                WHERE id = ? AND is_active = TRUE
                ON CONFLICT (user_id, product_id) DO NOTHING
                """, userId, productId);
    }

    @Override
    public int remove(long userId, long productId) throws SQLException {
        return JdbcUtil.update("""
                DELETE FROM wishlist_items
                WHERE user_id = ? AND product_id = ?
                """, userId, productId);
    }
}