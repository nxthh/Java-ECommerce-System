package org.marketplace.repository.impl;

import org.marketplace.config.DBConfig;
import org.marketplace.exception.DataAccessException;
import org.marketplace.model.User;
import org.marketplace.repository.UserRepository;
import org.marketplace.util.JdbcUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL implementation of {@link UserRepository}.
 */
public class PSQLUserRepository implements UserRepository {

    private final DBConfig db = DBConfig.getInstance();

    private static final String SELECT_BASE =
            "SELECT id, username, password_hash, full_name, role FROM users ";

    private static final String FIND_BY_USERNAME = SELECT_BASE + "WHERE username = ?";
    private static final String FIND_BY_ID        = SELECT_BASE + "WHERE id = ?";
    private static final String FIND_ALL           = SELECT_BASE + "ORDER BY id";
    private static final String FIND_BY_ROLE       = SELECT_BASE + "WHERE role = ? ORDER BY username";

    private static final String INSERT =
            "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE users SET username = ?, password_hash = ?, full_name = ?, role = ? WHERE id = ?";

    private static final String DELETE = "DELETE FROM users WHERE id = ?";

    // -----------------------------------------------------------------------

    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, FIND_BY_USERNAME, username);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find user by username", e);
        }
    }

    @Override
    public Optional<User> findById(long id) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, FIND_BY_ID, id);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find user id=" + id, e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch users", e);
        }
        return list;
    }

    @Override
    public List<User> findByRole(User.Role role) {
        List<User> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, FIND_BY_ROLE, role.name());
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch users by role=" + role, e);
        }
        return list;
    }

    @Override
    public User save(User user) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepareWithKeys(conn, INSERT,
                     user.getUsername(), user.getPasswordHash(),
                     user.getFullName(), user.getRole().name())) {
            stmt.executeUpdate();
            user.setId(JdbcUtil.getGeneratedKey(stmt));
            return user;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert user", e);
        }
    }

    @Override
    public boolean update(User user) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, UPDATE,
                     user.getUsername(), user.getPasswordHash(),
                     user.getFullName(), user.getRole().name(), user.getId())) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update user id=" + user.getId(), e);
        }
    }

    @Override
    public boolean delete(long id) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = JdbcUtil.prepare(conn, DELETE, id)) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete user id=" + id, e);
        }
    }

    // -----------------------------------------------------------------------

    private User map(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .username(rs.getString("username"))
                .passwordHash(rs.getString("password_hash"))
                .fullName(rs.getString("full_name"))
                .role(User.Role.valueOf(rs.getString("role")))
                .build();
    }
}
