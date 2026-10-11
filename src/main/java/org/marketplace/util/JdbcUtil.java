package org.marketplace.util;

import org.marketplace.config.DBConfig;
import org.marketplace.exception.DataAccessException;
import org.marketplace.model.TableData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcUtil {

    public static Connection open() {
        try {
            return DBConfig.getConnection();
        } catch (SQLException exception) {
            throw new DataAccessException(
                    "Cannot connect to the database.", exception
            );
        }
    }

    public static PreparedStatement prepare(
            Connection connection,
            String sql,
            Object... values
    ) throws SQLException {

        PreparedStatement statement = connection.prepareStatement(sql);

        try {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }

            return statement;

        } catch (SQLException exception) {
            statement.close();
            throw exception;
        }
    }

    public static int update(
            Connection connection,
            String sql,
            Object... values
    ) throws SQLException {

        try (PreparedStatement statement =
                     prepare(connection, sql, values)) {
            return statement.executeUpdate();
        }
    }

    public static int update(String sql, Object... values)
            throws SQLException {

        try (Connection connection = open()) {
            return update(connection, sql, values);
        }
    }

    public static long insertId(
            Connection connection,
            String sql,
            Object... values
    ) throws SQLException {

        try (PreparedStatement statement =
                     prepare(connection, sql, values);
             ResultSet result = statement.executeQuery()) {

            if (!result.next()) {
                throw new SQLException("Insert returned no ID.");
            }

            return result.getLong(1);
        }
    }

    public static TableData query(String sql, Object... values)
            throws SQLException {

        try (Connection connection = open();
             PreparedStatement statement =
                     prepare(connection, sql, values);
             ResultSet result = statement.executeQuery()) {

            ResultSetMetaData metadata = result.getMetaData();
            int count = metadata.getColumnCount();

            String[] headers = new String[count];
            List<String[]> rows = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                headers[i] = metadata.getColumnLabel(i + 1);
            }

            while (result.next()) {
                String[] row = new String[count];

                for (int i = 0; i < count; i++) {
                    String value = result.getString(i + 1);
                    row[i] = value == null ? "" : value;
                }

                rows.add(row);
            }

            return new TableData(headers, rows);
        }
    }

    public static void rollback(
            Connection connection,
            Exception original
    ) {
        try {
            connection.rollback();
        } catch (SQLException exception) {
            original.addSuppressed(exception);
        }
    }
}