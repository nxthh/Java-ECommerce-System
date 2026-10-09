package org.marketplace.repository.impl;

import org.marketplace.config.DBConfig;
import org.marketplace.exception.DataAccessException;
import org.marketplace.model.Order;
import org.marketplace.model.OrderItem;
import org.marketplace.repository.OrderRepository;
import org.marketplace.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL implementation of {@link OrderRepository}.
 * To be fleshed out by Phanit.
 */
public class PSQLOrderRepository implements OrderRepository {

    private final DBConfig db = DBConfig.getInstance();

    private static final String FIND_BY_CUSTOMER =
            "SELECT o.id, o.customer_id, o.status, o.total_amount, o.created_at, u.username AS customer_username " +
            "FROM   orders o JOIN users u ON u.id = o.customer_id " +
            "WHERE  o.customer_id = ? ORDER BY o.created_at DESC";

    private static final String FIND_ALL =
            "SELECT o.id, o.customer_id, o.status, o.total_amount, o.created_at, u.username AS customer_username " +
            "FROM   orders o JOIN users u ON u.id = o.customer_id ORDER BY o.created_at DESC";

    private static final String FIND_BY_ID =
            "SELECT o.id, o.customer_id, o.status, o.total_amount, o.created_at, u.username AS customer_username " +
            "FROM   orders o JOIN users u ON u.id = o.customer_id WHERE o.id = ?";

    private static final String FIND_ITEMS =
            "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.unit_price, p.name AS product_name " +
            "FROM   order_items oi JOIN products p ON p.id = oi.product_id WHERE oi.order_id = ?";

    private static final String INSERT_ORDER =
            "INSERT INTO orders (customer_id, status, total_amount) VALUES (?, ?, ?)";

    private static final String INSERT_ITEM =
            "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_STATUS =
            "UPDATE orders SET status = ? WHERE id = ?";

    @Override
    public List<Order> findByCustomer(long customerId) {
        return queryOrders(FIND_BY_CUSTOMER, customerId);
    }

    @Override
    public List<Order> findAll() {
        return queryOrders(FIND_ALL);
    }

    @Override
    public Optional<Order> findById(long id) {
        List<Order> results = queryOrders(FIND_BY_ID, id);
        if (results.isEmpty()) return Optional.empty();
        Order order = results.get(0);
        order.setItems(fetchItems(order.getId()));
        return Optional.of(order);
    }

    @Override
    public Order save(Order order) {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert order header
                try (PreparedStatement stmt = JdbcUtil.prepareWithKeys(conn, INSERT_ORDER,
                        order.getCustomerId(),
                        order.getStatus().name(),
                        order.getTotalAmount())) {
                    stmt.executeUpdate();
                    order.setId(JdbcUtil.getGeneratedKey(stmt));
                }
                // Insert order items
                if (order.getItems() != null) {
                    for (OrderItem item : order.getItems()) {
                        try (PreparedStatement stmt = JdbcUtil.prepareWithKeys(conn, INSERT_ITEM,
                                order.getId(), item.getProductId(),
                                item.getQuantity(), item.getUnitPrice())) {
                            stmt.executeUpdate();
                            item.setId(JdbcUtil.getGeneratedKey(stmt));
                            item.setOrderId(order.getId());
                        }
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save order", e);
        }
        return order;
    }

    @Override
    public boolean updateStatus(long orderId, Order.Status status) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, UPDATE_STATUS, status.name(), orderId)) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update order status", e);
        }
    }

    // -----------------------------------------------------------------------

    private List<Order> queryOrders(String sql, Object... params) {
        List<Order> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, sql, params);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapOrder(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query orders", e);
        }
        return list;
    }

    private List<OrderItem> fetchItems(long orderId) {
        List<OrderItem> items = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, FIND_ITEMS, orderId);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) items.add(mapItem(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch order items", e);
        }
        return items;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        return Order.builder()
                .id(rs.getLong("id"))
                .customerId(rs.getLong("customer_id"))
                .customerUsername(rs.getString("customer_username"))
                .status(Order.Status.valueOf(rs.getString("status")))
                .totalAmount(rs.getBigDecimal("total_amount"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }

    private OrderItem mapItem(ResultSet rs) throws SQLException {
        return OrderItem.builder()
                .id(rs.getLong("id"))
                .orderId(rs.getLong("order_id"))
                .productId(rs.getLong("product_id"))
                .productName(rs.getString("product_name"))
                .quantity(rs.getInt("quantity"))
                .unitPrice(rs.getBigDecimal("unit_price"))
                .build();
    }
}
