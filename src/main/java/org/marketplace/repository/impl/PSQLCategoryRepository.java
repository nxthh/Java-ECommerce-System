package org.marketplace.repository.impl;

import org.marketplace.config.DBConfig;
import org.marketplace.exception.DataAccessException;
import org.marketplace.model.Category;
import org.marketplace.repository.CategoryRepository;
import org.marketplace.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL implementation of {@link CategoryRepository}.
 */
public class PSQLCategoryRepository implements CategoryRepository {

    private final DBConfig db = DBConfig.getInstance();

    // -----------------------------------------------------------------------
    // Queries
    // -----------------------------------------------------------------------

    private static final String FIND_ALL =
            "SELECT id, name FROM categories ORDER BY name";

    private static final String FIND_BY_ID =
            "SELECT id, name FROM categories WHERE id = ?";

    private static final String FIND_BY_NAME =
            "SELECT id, name FROM categories WHERE LOWER(name) = LOWER(?)";

    private static final String INSERT =
            "INSERT INTO categories (name) VALUES (?) RETURNING id";

    private static final String UPDATE =
            "UPDATE categories SET name = ? WHERE id = ?";

    private static final String DELETE =
            "DELETE FROM categories WHERE id = ?";

    // -----------------------------------------------------------------------
    // Interface implementation
    // -----------------------------------------------------------------------

    @Override
    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch categories", e);
        }
        return list;
    }

    @Override
    public Optional<Category> findById(long id) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, FIND_BY_ID, id);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch category id=" + id, e);
        }
    }

    @Override
    public Optional<Category> findByName(String name) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, FIND_BY_NAME, name);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch category name=" + name, e);
        }
    }

    @Override
    public Category save(Category category) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, INSERT, category.getName());
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                category.setId(rs.getLong(1));
            }
            return category;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert category", e);
        }
    }

    @Override
    public boolean update(Category category) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, UPDATE,
                     category.getName(), category.getId())) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update category id=" + category.getId(), e);
        }
    }

    @Override
    public boolean delete(long id) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, DELETE, id)) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete category id=" + id, e);
        }
    }

    // -----------------------------------------------------------------------

    private Category map(ResultSet rs) throws SQLException {
        return Category.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
    }
}
