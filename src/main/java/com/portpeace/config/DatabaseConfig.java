package com.portpeace.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final String CONFIG_FILE = System.getProperty("user.home") + "/.portpeace/config.properties";
    private static HikariDataSource dataSource;

    // Default configuration
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "portpeace";
    private static final String DEFAULT_USERNAME = "portpeace";
    private static final String DEFAULT_PASSWORD = "Portpeace##1";

    static {
        // Suppress debug logs from third-party libraries
        suppressDebugLogs();
    }

    private static void suppressDebugLogs() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLogger("com.zaxxer.hikari").setLevel(ch.qos.logback.classic.Level.WARN);
        context.getLogger("com.zaxxer").setLevel(ch.qos.logback.classic.Level.WARN);
        context.getLogger("com.mysql.cj").setLevel(ch.qos.logback.classic.Level.WARN);
    }


    public static void initialize() throws SQLException {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        Properties props = loadConfiguration();
        
        String host = props.getProperty("db.host", DEFAULT_HOST);
        String port = props.getProperty("db.port", DEFAULT_PORT);
        String database = props.getProperty("db.database", DEFAULT_DATABASE);
        String username = props.getProperty("db.username", DEFAULT_USERNAME);
        String password = props.getProperty("db.password", DEFAULT_PASSWORD);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + 
                         "?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            dataSource = new HikariDataSource(config);
            initializeSchema();
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new SQLException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initialize();
        }
        return dataSource.getConnection();
    }


    private static void initializeSchema() throws SQLException {
        String createPortAllocationsTable =
    "CREATE TABLE IF NOT EXISTS port_allocations (" +
    "id INT AUTO_INCREMENT PRIMARY KEY, " +
    "service_name VARCHAR(255) NOT NULL UNIQUE, " +
    "port_number INT NOT NULL, " +
    "username VARCHAR(255) NOT NULL, " +
    "hostname VARCHAR(255) NOT NULL, " +
    "status ENUM('ACTIVE', 'INACTIVE', 'RESERVED') DEFAULT 'ACTIVE', " +
    "allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
    "last_used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
    "description TEXT, " +
    "INDEX idx_port (port_number), " +
    "INDEX idx_service (service_name), " +
    "INDEX idx_status (status)" +
    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";


        String createPortHistoryTable =
    "CREATE TABLE IF NOT EXISTS port_history (" +
    "id INT AUTO_INCREMENT PRIMARY KEY, " +
    "service_name VARCHAR(255) NOT NULL, " +
    "port_number INT NOT NULL, " +
    "username VARCHAR(255) NOT NULL, " +
    "hostname VARCHAR(255) NOT NULL, " +
    "action ENUM('ALLOCATED', 'FREED', 'CONFLICT', 'AUTO_CLEANED') NOT NULL, " +
    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
    "details TEXT, " +
    "INDEX idx_service (service_name), " +
    "INDEX idx_timestamp (timestamp)" +
    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";


        String createPreferencesTable =
    "CREATE TABLE IF NOT EXISTS user_preferences (" +
    "id INT AUTO_INCREMENT PRIMARY KEY, " +
    "username VARCHAR(255) NOT NULL, " +
    "hostname VARCHAR(255) NOT NULL, " +
    "preferred_port_range_start INT DEFAULT 3000, " +
    "preferred_port_range_end INT DEFAULT 9999, " +
    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
    "UNIQUE KEY unique_user_host (username, hostname)" +
    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";


        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createPortAllocationsTable);
            stmt.execute(createPortHistoryTable);
            stmt.execute(createPreferencesTable);
        }
    }

  
    private static Properties loadConfiguration() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);

        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                props.load(input);
            } catch (IOException e) {
                logger.warn("Failed to load configuration file, using defaults", e);
            }
        } else {
            // Create default configuration
            createDefaultConfiguration();
        }

        return props;
    }

  
    private static void createDefaultConfiguration() {
        File configDir = new File(System.getProperty("user.home") + "/.portpeace");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        Properties props = new Properties();
        props.setProperty("db.host", DEFAULT_HOST);
        props.setProperty("db.port", DEFAULT_PORT);
        props.setProperty("db.database", DEFAULT_DATABASE);
        props.setProperty("db.username", DEFAULT_USERNAME);
        props.setProperty("db.password", DEFAULT_PASSWORD);

        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, "PortPeace Configuration");
        } catch (IOException e) {
            logger.error("Failed to create default configuration", e);
        }
    }

   
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
}
