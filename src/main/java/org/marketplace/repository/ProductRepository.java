package org.marketplace.repository;

import org.marketplace.model.*;
import java.sql.*;

public interface ProductRepository {
    TableData search(String keyword, long categoryId, long sellerId)
            throws SQLException;

    Product findById(Connection connection, long id, boolean lock)
            throws SQLException;

    void save(Connection connection, Product product) throws SQLException;

    int update(Product product) throws SQLException;

    int setActive(long id, long sellerId, boolean active)
            throws SQLException;

    int reduceStock(Connection connection, long id, int quantity)
            throws SQLException;

    TableData exportBySeller(long sellerId) throws SQLException;
}