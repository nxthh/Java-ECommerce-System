package org.marketplace.repository.impl;

import org.marketplace.model.*;
import org.marketplace.repository.CartRepository;
import org.marketplace.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PSQLCartRepository implements CartRepository {

    @Override
    public TableData findByUser(long userId) throws SQLException {
        return JdbcUtil.query("""
                SELECT p.id AS product_id, p.name, p.price,
                       ci.quantity,
                       p.price * ci.quantity AS subtotal,
                       p.stock, p.is_active
                FROM cart_items ci
                JOIN products p ON p.id = ci.product_id
                WHERE ci.user_id = ?
                ORDER BY p.id
                """, userId);
    }

    @Override
    public List<CartItem> findItems(
            Connection connection, long userId
    ) throws SQLException {

        List<CartItem> items = new ArrayList<>();

        try (PreparedStatement statement = JdbcUtil.prepare(
                connection,
                """
                SELECT user_id, product_id, quantity
                FROM cart_items
                WHERE user_id = ?
                ORDER BY product_id
                FOR UPDATE
                """,
                userId
        );
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                CartItem item = new CartItem();
                item.setUserId(result.getLong("user_id"));
                item.setProductId(result.getLong("product_id"));
                item.setQuantity(result.getInt("quantity"));

                items.add(item);
            }
        }

        return items;
    }

    @Override
    public void setQuantity(
            Connection connection, long userId,
            long productId, int quantity
    ) throws SQLException {

        JdbcUtil.update(connection, """
                INSERT INTO cart_items(user_id, product_id, quantity)
                VALUES (?, ?, ?)
                ON CONFLICT (user_id, product_id)
                DO UPDATE SET quantity = EXCLUDED.quantity
                """, userId, productId, quantity);
    }

    @Override
    public int remove(
            Connection connection, long userId, long productId
    ) throws SQLException {

        return JdbcUtil.update(connection, """
                DELETE FROM cart_items
                WHERE user_id = ? AND product_id = ?
                """, userId, productId);
    }
}