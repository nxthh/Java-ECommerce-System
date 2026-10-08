package org.marketplace.repository.impl;

import org.marketplace.model.TableData;
import org.marketplace.model.User;
import org.marketplace.repository.UserRepository;
import org.marketplace.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PSQLUserRepository implements UserRepository {

    @Override
    public User findByUsername(String username) throws SQLException {
        String sql = """
                SELECT id, username, password_hash, full_name, role
                FROM users
                WHERE username = ?
                """;

        try (
                Connection connection = JdbcUtil.open();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();

                    user.setId(resultSet.getLong("id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setPasswordHash(
                            resultSet.getString("password_hash")
                    );
                    user.setFullName(resultSet.getString("full_name"));
                    user.setRole(resultSet.getString("role"));

                    return user;
                }
            }
        }

        return null;
    }

    @Override
    public void save(User user) throws SQLException {
        String sql = """
                INSERT INTO users (
                    username,
                    password_hash,
                    full_name,
                    role
                )
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;

        try (
                Connection connection = JdbcUtil.open();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getRole());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Could not create user.");
                }

                user.setId(resultSet.getLong("id"));
            }
        }
    }

    @Override
    public TableData findById(long id) throws SQLException {
        String sql = """
                SELECT id, username, full_name, role
                FROM users
                WHERE id = ?
                """;

        return JdbcUtil.query(sql, id);
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = """
                SELECT id, username, full_name, role
                FROM users
                ORDER BY id
                """;

        List<User> users = new ArrayList<>();

        try (
                Connection connection = JdbcUtil.open();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                User user = new User();

                user.setId(resultSet.getLong("id"));
                user.setUsername(resultSet.getString("username"));
                user.setFullName(resultSet.getString("full_name"));
                user.setRole(resultSet.getString("role"));

                users.add(user);
            }
        }

        return users;
    }

    @Override
    public int updateName(long id, String fullName)
            throws SQLException {

        String sql = """
                UPDATE users
                SET full_name = ?
                WHERE id = ?
                """;

        try (
                Connection connection = JdbcUtil.open();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, fullName);
            statement.setLong(2, id);

            return statement.executeUpdate();
        }
    }

    @Override
    public void lock(Connection connection, long userId)
            throws SQLException {

        String sql = """
                SELECT id
                FROM users
                WHERE id = ?
                FOR UPDATE
                """;

        // Use the caller's transaction connection.
        // Do not close or commit that connection here.
        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("User not found.");
                }
            }
        }
    }
}