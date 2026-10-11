package org.marketplace.repository.impl;

import org.marketplace.model.TableData;
import org.marketplace.repository.ReviewRepository;
import org.marketplace.util.JdbcUtil;

import java.sql.SQLException;

public class PSQLReviewRepository implements ReviewRepository {

    @Override
    public TableData findByProduct(long productId)
            throws SQLException {

        return JdbcUtil.query("""
                SELECT u.username, r.rating, r.comment, r.created_at
                FROM reviews r
                JOIN users u ON u.id = r.user_id
                WHERE r.product_id = ?
                ORDER BY r.created_at DESC
                """, productId);
    }

    @Override
    public int save(
            long userId, long productId, int rating, String comment
    ) throws SQLException {

        return JdbcUtil.update("""
                INSERT INTO reviews(user_id, product_id, rating, comment)
                SELECT ?, ?, ?, ?
                WHERE EXISTS (
                    SELECT 1
                    FROM orders o
                    JOIN order_items oi ON oi.order_id = o.id
                    WHERE o.customer_id = ?
                      AND oi.product_id = ?
                      AND o.status IN ('PAID', 'COMPLETED')
                )
                ON CONFLICT (user_id, product_id)
                DO UPDATE SET
                    rating = EXCLUDED.rating,
                    comment = EXCLUDED.comment,
                    created_at = CURRENT_TIMESTAMP
                """, userId, productId, rating, comment, userId, productId);
    }
}