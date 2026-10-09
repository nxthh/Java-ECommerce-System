package org.marketplace.repository.impl;

import org.marketplace.config.DBConfig;
import org.marketplace.exception.DataAccessException;
import org.marketplace.model.Payment;
import org.marketplace.repository.PaymentRepository;
import org.marketplace.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL implementation of {@link PaymentRepository}.
 * To be fleshed out by Phanit.
 */
public class PSQLPaymentRepository implements PaymentRepository {

    private final DBConfig db = DBConfig.getInstance();

    private static final String FIND_BY_ORDER =
            "SELECT id, order_id, amount, method, status, created_at FROM payments WHERE order_id = ?";
    private static final String FIND_BY_ID =
            "SELECT id, order_id, amount, method, status, created_at FROM payments WHERE id = ?";
    private static final String INSERT =
            "INSERT INTO payments (order_id, amount, method, status) VALUES (?, ?, ?, ?)";

    @Override
    public List<Payment> findByOrder(long orderId) {
        List<Payment> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, FIND_BY_ORDER, orderId);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch payments for order=" + orderId, e);
        }
        return list;
    }

    @Override
    public Optional<Payment> findById(long id) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, FIND_BY_ID, id);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch payment id=" + id, e);
        }
    }

    @Override
    public Payment save(Payment payment) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepareWithKeys(conn, INSERT,
                     payment.getOrderId(),
                     payment.getAmount(),
                     payment.getMethod().name(),
                     payment.getStatus().name())) {
            stmt.executeUpdate();
            payment.setId(JdbcUtil.getGeneratedKey(stmt));
            return payment;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save payment", e);
        }
    }

    private Payment map(ResultSet rs) throws SQLException {
        return Payment.builder()
                .id(rs.getLong("id"))
                .orderId(rs.getLong("order_id"))
                .amount(rs.getBigDecimal("amount"))
                .method(Payment.Method.valueOf(rs.getString("method")))
                .status(Payment.Status.valueOf(rs.getString("status")))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
