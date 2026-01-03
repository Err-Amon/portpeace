package com.portpeace;

import com.portpeace.cli.CommandLineInterface;
import com.portpeace.config.DatabaseConfig;
import com.portpeace.service.PortManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Main application entry point for PortPeace
 */
public class PortPeaceApplication {
    private static final Logger logger = LoggerFactory.getLogger(PortPeaceApplication.class);

    public static void main(String[] args) {
        try {
            // Initialize database
            logger.info("Initializing PortPeace...");
            DatabaseConfig.initialize();

            // Test database connection
            if (!DatabaseConfig.testConnection()) {
                System.err.println("Error: Cannot connect to database.");
                System.err.println("Please ensure MySQL is running and configuration is correct.");
                System.err.println("Configuration file: ~/.portpeace/config.properties");
                System.exit(1);
            }

            // Create service and CLI
            PortManagementService service = new PortManagementService();
            CommandLineInterface cli = new CommandLineInterface(service);

            // Process command
            cli.processCommand(args);

        } catch (SQLException e) {
            logger.error("Database error", e);
            System.err.println("Database Error: " + e.getMessage());
            System.err.println();
            System.err.println("Please check:");
            System.err.println("1. MySQL server is running");
            System.err.println("2. Database credentials in ~/.portpeace/config.properties");
            System.err.println("3. User has proper permissions");
            System.exit(1);
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } finally {
            // Cleanup
            DatabaseConfig.close();
        }
    }
}
