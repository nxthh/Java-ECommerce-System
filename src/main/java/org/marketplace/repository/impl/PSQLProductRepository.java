package org.marketplace.repository.impl;

import org.marketplace.model.*;
import org.marketplace.repository.ProductRepository;
import org.marketplace.util.JdbcUtil;

import java.sql.*;

public class PSQLProductRepository implements ProductRepository {

    @Override
    public TableData search(
            String keyword, long categoryId, long sellerId
    ) throws SQLException {

        return JdbcUtil.query("""
                SELECT p.id, p.name, c.name AS category,
                       u.username AS seller,
                       p.price, p.stock, p.is_active
                FROM products p
                JOIN categories c ON c.id = p.category_id
                JOIN users u ON u.id = p.seller_id
                WHERE p.name ILIKE ?
                  AND (? = 0 OR p.category_id = ?)
                  AND (
                      (? = 0 AND p.is_active = TRUE)
                      OR p.seller_id = ?
                  )
                ORDER BY p.id
                """,
                "%" + keyword + "%",
                categoryId, categoryId, sellerId, sellerId);
    }

    @Override
    public Product findById(
            Connection connection, long id, boolean lock
    ) throws SQLException {

        String sql = "SELECT * FROM products WHERE id = ?";

        if (lock) {
            sql += " FOR UPDATE";
        }

        try (PreparedStatement statement =
                     JdbcUtil.prepare(connection, sql, id);
             ResultSet result = statement.executeQuery()) {

            if (!result.next()) {
                return null;
            }

            Product product = new Product();
            product.setId(result.getLong("id"));
            product.setSellerId(result.getLong("seller_id"));
            product.setCategoryId(result.getLong("category_id"));
            product.setName(result.getString("name"));
            product.setPrice(result.getBigDecimal("price"));
            product.setStock(result.getInt("stock"));
            product.setActive(result.getBoolean("is_active"));

            return product;
        }
    }

    @Override
    public void save(Connection connection, Product product)
            throws SQLException {

        JdbcUtil.update(connection, """
                INSERT INTO products(
                    seller_id, category_id, name, price, stock, is_active
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                product.getSellerId(), product.getCategoryId(),
                product.getName(), product.getPrice(),
                product.getStock(), product.isActive());
    }

    @Override
    public int update(Product product) throws SQLException {
        return JdbcUtil.update("""
                UPDATE products
                SET category_id = ?, name = ?, price = ?, stock = ?
                WHERE id = ? AND seller_id = ?
                """,
                product.getCategoryId(), product.getName(),
                product.getPrice(), product.getStock(),
                product.getId(), product.getSellerId());
    }

    @Override
    public int setActive(long id, long sellerId, boolean active)
            throws SQLException {

        return JdbcUtil.update("""
                UPDATE products SET is_active = ?
                WHERE id = ? AND seller_id = ?
                """, active, id, sellerId);
    }

    @Override
    public int reduceStock(
            Connection connection, long id, int quantity
    ) throws SQLException {

        return JdbcUtil.update(connection, """
                UPDATE products SET stock = stock - ?
                WHERE id = ? AND stock >= ? AND is_active = TRUE
                """, quantity, id, quantity);
    }

    @Override
    public TableData exportBySeller(long sellerId) throws SQLException {
        return JdbcUtil.query("""
                SELECT category_id, name, price, stock, is_active
                FROM products WHERE seller_id = ?
                ORDER BY id
                """, sellerId);
    }
}