package org.marketplace.repository;

import org.marketplace.model.*;
import java.math.BigDecimal;
import java.sql.*;

public interface OrderRepository {
    long create(Connection connection, long customerId, BigDecimal total)
            throws SQLException;

    void addItem(Connection connection, long orderId,
                 Product product, int quantity) throws SQLException;

    void updateStatus(Connection connection, long id, String status)
            throws SQLException;

    TableData findByUser(User user) throws SQLException;

    TableData details(User user, long orderId) throws SQLException;

    int complete(long orderId) throws SQLException;
}