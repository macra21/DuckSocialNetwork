package com.org.ddd.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnectionManager {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream input = DatabaseConnectionManager.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("CRITICAL ERROR: 'config.properties' not found.");
                throw new RuntimeException("config.properties not found in classpath");
            }
            props.load(input);
            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.pass");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    private DatabaseConnectionManager() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            // If I decide to use Listen/Notify from postgresql i will return the same connection
            // Without that the repos close the connection automatically after every query
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Error getting database connection: " + e.getMessage());
            throw e;
        }
    }
}
