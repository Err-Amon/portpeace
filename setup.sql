-- PortPeace Database Setup Script
-- Run this script to set up the database and user

-- Create database
CREATE DATABASE IF NOT EXISTS portpeace
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Create user (change password in production!)
CREATE USER IF NOT EXISTS 'portpeace'@'localhost' IDENTIFIED BY 'Portpeace123##';

-- Grant privileges
GRANT ALL PRIVILEGES ON portpeace.* TO 'portpeace'@'localhost';
FLUSH PRIVILEGES;

-- Use the database
USE portpeace;

-- Create port_allocations table
CREATE TABLE IF NOT EXISTS port_allocations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL UNIQUE,
    port_number INT NOT NULL,
    username VARCHAR(255) NOT NULL,
    hostname VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'RESERVED') DEFAULT 'ACTIVE',
    allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    description TEXT,
    INDEX idx_port (port_number),
    INDEX idx_service (service_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create port_history table
CREATE TABLE IF NOT EXISTS port_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL,
    port_number INT NOT NULL,
    username VARCHAR(255) NOT NULL,
    hostname VARCHAR(255) NOT NULL,
    action ENUM('ALLOCATED', 'FREED', 'CONFLICT', 'AUTO_CLEANED') NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    INDEX idx_service (service_name),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create user_preferences table
CREATE TABLE IF NOT EXISTS user_preferences (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    hostname VARCHAR(255) NOT NULL,
    preferred_port_range_start INT DEFAULT 3000,
    preferred_port_range_end INT DEFAULT 9999,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_host (username, hostname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Verify setup
SELECT 'Database setup completed successfully!' AS status;
SHOW TABLES;

-- Show grants for portpeace user
SHOW GRANTS FOR 'portpeace'@'localhost';
