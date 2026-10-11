package org.marketplace.repository.impl;

import org.marketplace.model.*;
import org.marketplace.repository.PaymentRepository;
import org.marketplace.util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.*;

public class PSQLPaymentRepository implements PaymentRepository {

    @Override
    public void save(
            Connection connection, long orderId,
            BigDecimal amount, String method, String status
    ) throws SQLException {

        JdbcUtil.update(connection, """
                INSERT INTO payments(order_id, amount, method, status)
                VALUES (?, ?, ?, ?)
                """, orderId, amount, method, status);
    }

    @Override
    public TableData findByUser(User user) throws SQLException {
        return JdbcUtil.query("""
                SELECT p.id, p.order_id, p.amount,
                       p.method, p.status, p.created_at
                FROM payments p
                JOIN orders o ON o.id = p.order_id
                WHERE ? = 'ADMIN' OR o.customer_id = ?
                ORDER BY p.id DESC
                """, user.getRole(), user.getId());
    }
}