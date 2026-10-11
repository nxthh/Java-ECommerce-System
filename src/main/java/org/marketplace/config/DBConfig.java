package org.marketplace.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConfig {
    private static final  Properties properties = new Properties();

    static {
        try (InputStream input = DBConfig.class.getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new IllegalStateException("db.properties not found.");
            }

            properties.load(input);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Cannot read database configuration.", exception
            );
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.username"),
                properties.getProperty("db.password")
        );
    }

}
