package org.marketplace.repository.impl;

import org.marketplace.config.DBConfig;
import org.marketplace.exception.DataAccessException;
import org.marketplace.model.Review;
import org.marketplace.repository.ReviewRepository;
import org.marketplace.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL implementation of {@link ReviewRepository}.
 */
public class PSQLReviewRepository implements ReviewRepository {

    private final DBConfig db = DBConfig.getInstance();

    private static final String SELECT_BASE =
            "SELECT r.id, r.user_id, r.product_id, r.rating, r.comment, r.created_at, " +
            "       u.username, p.name AS product_name " +
            "FROM   reviews r " +
            "JOIN   users    u ON u.id = r.user_id " +
            "JOIN   products p ON p.id = r.product_id ";

    private static final String FIND_BY_PRODUCT  = SELECT_BASE + "WHERE r.product_id = ? ORDER BY r.created_at DESC";
    private static final String FIND_BY_USER      = SELECT_BASE + "WHERE r.user_id = ? ORDER BY r.created_at DESC";
    private static final String FIND_BY_ID        = SELECT_BASE + "WHERE r.id = ?";
    private static final String FIND_BY_USER_PROD = SELECT_BASE + "WHERE r.user_id = ? AND r.product_id = ?";

    private static final String INSERT =
            "INSERT INTO reviews (user_id, product_id, rating, comment) VALUES (?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE reviews SET rating = ?, comment = ? WHERE id = ?";
    private static final String DELETE =
            "DELETE FROM reviews WHERE id = ?";

    @Override
    public List<Review> findByProduct(long productId) {
        return query(FIND_BY_PRODUCT, productId);
    }

    @Override
    public List<Review> findByUser(long userId) {
        return query(FIND_BY_USER, userId);
    }

    @Override
    public Optional<Review> findById(long id) {
        List<Review> r = query(FIND_BY_ID, id);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    @Override
    public Optional<Review> findByUserAndProduct(long userId, long productId) {
        List<Review> r = query(FIND_BY_USER_PROD, userId, productId);
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    @Override
    public Review save(Review review) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepareWithKeys(conn, INSERT,
                     review.getUserId(), review.getProductId(),
                     review.getRating(), review.getComment())) {
            stmt.executeUpdate();
            review.setId(JdbcUtil.getGeneratedKey(stmt));
            return review;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save review", e);
        }
    }

    @Override
    public boolean update(Review review) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, UPDATE,
                     review.getRating(), review.getComment(), review.getId())) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update review id=" + review.getId(), e);
        }
    }

    @Override
    public boolean delete(long id) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, DELETE, id)) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete review id=" + id, e);
        }
    }

    private List<Review> query(String sql, Object... params) {
        List<Review> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, sql, params);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Review query failed", e);
        }
        return list;
    }

    private Review map(ResultSet rs) throws SQLException {
        return Review.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .username(rs.getString("username"))
                .productId(rs.getLong("product_id"))
                .productName(rs.getString("product_name"))
                .rating(rs.getInt("rating"))
                .comment(rs.getString("comment"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
