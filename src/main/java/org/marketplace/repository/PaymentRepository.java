package org.marketplace.repository;

import org.marketplace.model.*;
import java.math.BigDecimal;
import java.sql.*;

public interface PaymentRepository {
    void save(Connection connection, long orderId,
              BigDecimal amount, String method, String status)
            throws SQLException;

    TableData findByUser(User user) throws SQLException;
}