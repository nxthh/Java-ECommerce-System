package org.marketplace.repository.impl;

import org.marketplace.model.*;
import org.marketplace.repository.OrderRepository;
import org.marketplace.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.*;

public class PSQLOrderRepository implements OrderRepository {

    @Override
    public long create(
            Connection connection, long customerId, BigDecimal total
    ) throws SQLException {

        return JdbcUtil.insertId(connection, """
                INSERT INTO orders(customer_id, status, total_amount)
                VALUES (?, 'PENDING', ?)
                RETURNING id
                """, customerId, total);
    }

    @Override
    public void addItem(
            Connection connection, long orderId,
            Product product, int quantity
    ) throws SQLException {

        JdbcUtil.update(connection, """
                INSERT INTO order_items(
                    order_id, product_id, quantity, unit_price
                )
                VALUES (?, ?, ?, ?)
                """,
                orderId, product.getId(), quantity, product.getPrice());
    }

    @Override
    public void updateStatus(
            Connection connection, long id, String status
    ) throws SQLException {

        JdbcUtil.update(connection,
                "UPDATE orders SET status = ? WHERE id = ?",
                status, id);
    }

    @Override
    public TableData findByUser(User user) throws SQLException {
        if ("ADMIN".equals(user.getRole())) {
            return JdbcUtil.query("""
                    SELECT id, customer_id, status,
                           total_amount, created_at
                    FROM orders ORDER BY id DESC
                    """);
        }

        if ("SELLER".equals(user.getRole())) {
            return JdbcUtil.query("""
                    SELECT o.id, o.status,
                           SUM(oi.quantity * oi.unit_price) AS seller_total,
                           o.created_at
                    FROM orders o
                    JOIN order_items oi ON oi.order_id = o.id
                    JOIN products p ON p.id = oi.product_id
                    WHERE p.seller_id = ?
                    GROUP BY o.id
                    ORDER BY o.id DESC
                    """, user.getId());
        }

        return JdbcUtil.query("""
                SELECT id, status, total_amount, created_at
                FROM orders
                WHERE customer_id = ?
                ORDER BY id DESC
                """, user.getId());
    }

    @Override
    public TableData details(User user, long orderId)
            throws SQLException {

        return JdbcUtil.query("""
                SELECT p.id AS product_id, p.name,
                       oi.quantity, oi.unit_price,
                       oi.quantity * oi.unit_price AS subtotal
                FROM order_items oi
                JOIN orders o ON o.id = oi.order_id
                JOIN products p ON p.id = oi.product_id
                WHERE o.id = ?
                  AND (
                      ? = 'ADMIN'
                      OR (? = 'CUSTOMER' AND o.customer_id = ?)
                      OR (? = 'SELLER' AND p.seller_id = ?)
                  )
                ORDER BY oi.id
                """,
                orderId, user.getRole(),
                user.getRole(), user.getId(),
                user.getRole(), user.getId());
    }

    @Override
    public int complete(long orderId) throws SQLException {
        return JdbcUtil.update("""
                UPDATE orders SET status = 'COMPLETED'
                WHERE id = ? AND status = 'PAID'
                """, orderId);
    }
}