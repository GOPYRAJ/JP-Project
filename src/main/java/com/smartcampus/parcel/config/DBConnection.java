package com.smartcampus.parcel.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection provides centralized access to the MySQL database.
 * Database credentials are read securely from db.properties on the classpath.
 */
public class DBConnection {

    private static String url;
    private static String username;
    private static String password;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                System.err.println("[DBConnection] ERROR: db.properties file not found on classpath!");
                System.err.println("[DBConnection] Please create src/main/resources/db.properties based on db.properties.example.");
                return;
            }
            props.load(in);

            // Support both db.url and url prefixes
            url = props.getProperty("db.url", props.getProperty("url"));
            username = props.getProperty("db.username", props.getProperty("username"));
            password = props.getProperty("db.password", props.getProperty("password"));

            // Ensure MySQL JDBC Driver is registered
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (IOException e) {
            System.err.println("[DBConnection] ERROR reading db.properties: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] ERROR: MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * Obtains a new database connection.
     *
     * @return active java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (url == null || username == null) {
            // Attempt to reload if it wasn't available at class loading time
            loadProperties();
            if (url == null || username == null) {
                throw new SQLException("Database credentials not properly configured in db.properties.");
            }
        }
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Quick helper method to verify database connectivity.
     *
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Test connection failed: " + e.getMessage());
            return false;
        }
    }
}
