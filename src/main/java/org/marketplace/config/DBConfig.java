package org.marketplace.config;

import org.marketplace.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Loads database connection properties from {@code db.properties} on the
 * classpath and provides a plain JDBC {@link Connection}.
 */
public class DBConfig {

    private static final String PROPERTIES_FILE = "db.properties";

    private final String url;
    private final String username;
    private final String password;

    /** Singleton instance – initialised once, reused throughout the app. */
    private static DBConfig instance;

    private DBConfig() {
        Properties props = new Properties();
        try (InputStream in = DBConfig.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {
            if (in == null) {
                throw new DataAccessException(
                        "Cannot find " + PROPERTIES_FILE + " on the classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new DataAccessException("Failed to load " + PROPERTIES_FILE, e);
        }

        this.url      = props.getProperty("db.url");
        this.username = props.getProperty("db.username");
        this.password = props.getProperty("db.password");
    }

    /** Returns the singleton {@link DBConfig}. */
    public static synchronized DBConfig getInstance() {
        if (instance == null) {
            instance = new DBConfig();
        }
        return instance;
    }

    /**
     * Opens and returns a new JDBC {@link Connection}.
     *
     * @throws DataAccessException if the connection cannot be established.
     */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to open database connection", e);
        }
    }
}
