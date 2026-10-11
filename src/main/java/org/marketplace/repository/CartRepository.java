package org.marketplace.repository;

import org.marketplace.model.*;
import java.sql.*;
import java.util.List;

public interface CartRepository {
    TableData findByUser(long userId) throws SQLException;

    List<CartItem> findItems(Connection connection, long userId)
            throws SQLException;

    void setQuantity(Connection connection, long userId,
                     long productId, int quantity) throws SQLException;

    int remove(Connection connection, long userId, long productId)
            throws SQLException;
}