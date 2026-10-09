package org.marketplace.util;

import org.marketplace.exception.DataAccessException;

import java.sql.*;

/**
 * Thin JDBC helper used by all repository implementations.
 * Provides auto-closeable wrappers to avoid repetitive try/catch blocks.
 */
public class JdbcUtil {

    private JdbcUtil() {}

    /**
     * Executes a query, returning a {@link ResultSet}.
     * The caller is responsible for closing the statement and connection.
     */
    public static PreparedStatement prepare(Connection conn, String sql, Object... params)
            throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        bindParams(stmt, params);
        return stmt;
    }

    /**
     * Executes a query that returns generated keys (INSERT).
     * The caller is responsible for closing the statement and connection.
     */
    public static PreparedStatement prepareWithKeys(Connection conn, String sql, Object... params)
            throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        bindParams(stmt, params);
        return stmt;
    }

    /**
     * Retrieves the first generated key from a statement executed against
     * a SERIAL / IDENTITY column.
     *
     * @throws DataAccessException if no key was returned.
     */
    public static long getGeneratedKey(PreparedStatement stmt) throws SQLException {
        try (ResultSet keys = stmt.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getLong(1);
            }
        }
        throw new DataAccessException("No generated key returned by the database");
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private static void bindParams(PreparedStatement stmt, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }
}
