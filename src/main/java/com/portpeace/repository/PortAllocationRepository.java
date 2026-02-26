package com.portpeace.repository;

import com.portpeace.config.DatabaseConfig;
import com.portpeace.model.PortAllocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class PortAllocationRepository {
    private static final Logger logger = LoggerFactory.getLogger(PortAllocationRepository.class);

   
    public boolean save(PortAllocation allocation) {
        String sql =
    "INSERT INTO port_allocations " +
    "(service_name, port_number, username, hostname, status, description) " +
    "VALUES (?, ?, ?, ?, ?, ?) " +
    "ON DUPLICATE KEY UPDATE " +
    "port_number = VALUES(port_number), " +
    "status = VALUES(status), " +
    "last_used_at = CURRENT_TIMESTAMP, " +
    "description = VALUES(description)";


        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, allocation.getServiceName());
            pstmt.setInt(2, allocation.getPortNumber());
            pstmt.setString(3, allocation.getUsername());
            pstmt.setString(4, allocation.getHostname());
            pstmt.setString(5, allocation.getStatus().name());
            pstmt.setString(6, allocation.getDescription());

            int rows = pstmt.executeUpdate();
            logger.info("Saved allocation: {}", allocation);
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Failed to save allocation: {}", allocation, e);
            return false;
        }
    }

    
    public Optional<PortAllocation> findByServiceName(String serviceName) {
        String sql = "SELECT * FROM port_allocations WHERE service_name = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, serviceName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToAllocation(rs));
            }

        } catch (SQLException e) {
            logger.error("Failed to find allocation by service: {}", serviceName, e);
        }

        return Optional.empty();
    }

    
    public Optional<PortAllocation> findByPortNumber(int portNumber) {
        String sql = "SELECT * FROM port_allocations WHERE port_number = ? AND status = 'ACTIVE'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, portNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToAllocation(rs));
            }

        } catch (SQLException e) {
            logger.error("Failed to find allocation by port: {}", portNumber, e);
        }

        return Optional.empty();
    }

   
    public List<PortAllocation> findAll() {
        List<PortAllocation> allocations = new ArrayList<>();
        String sql = "SELECT * FROM port_allocations ORDER BY port_number";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                allocations.add(mapResultSetToAllocation(rs));
            }

        } catch (SQLException e) {
            logger.error("Failed to retrieve all allocations", e);
        }

        return allocations;
    }

  
    public List<PortAllocation> findByStatus(PortAllocation.Status status) {
        List<PortAllocation> allocations = new ArrayList<>();
        String sql = "SELECT * FROM port_allocations WHERE status = ? ORDER BY port_number";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                allocations.add(mapResultSetToAllocation(rs));
            }

        } catch (SQLException e) {
            logger.error("Failed to retrieve allocations by status: {}", status, e);
        }

        return allocations;
    }

    
    public boolean deleteByServiceName(String serviceName) {
        String sql = "DELETE FROM port_allocations WHERE service_name = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, serviceName);
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                logger.info("Deleted allocation for service: {}", serviceName);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Failed to delete allocation: {}", serviceName, e);
        }

        return false;
    }

   
    public boolean updateStatus(String serviceName, PortAllocation.Status status) {
        String sql = "UPDATE port_allocations SET status = ?, last_used_at = CURRENT_TIMESTAMP WHERE service_name = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setString(2, serviceName);
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                logger.info("Updated status for service {} to {}", serviceName, status);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Failed to update status for service: {}", serviceName, e);
        }

        return false;
    }

    
    public int cleanupInactive(int daysOld) {
        String sql = "DELETE FROM port_allocations WHERE status = 'INACTIVE' AND last_used_at < DATE_SUB(NOW(), INTERVAL ? DAY)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, daysOld);
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                logger.info("Cleaned up {} inactive allocations", rows);
            }
            
            return rows;

        } catch (SQLException e) {
            logger.error("Failed to cleanup inactive allocations", e);
            return 0;
        }
    }

    public void logHistory(String serviceName, int portNumber, String username, 
                          String hostname, String action, String details) {
        String sql =
    "INSERT INTO port_history " +
    "(service_name, port_number, username, hostname, action, details) " +
    "VALUES (?, ?, ?, ?, ?, ?)";


        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, serviceName);
            pstmt.setInt(2, portNumber);
            pstmt.setString(3, username);
            pstmt.setString(4, hostname);
            pstmt.setString(5, action);
            pstmt.setString(6, details);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Failed to log history", e);
        }
    }

   
    private PortAllocation mapResultSetToAllocation(ResultSet rs) throws SQLException {
        PortAllocation allocation = new PortAllocation();
        allocation.setId(rs.getInt("id"));
        allocation.setServiceName(rs.getString("service_name"));
        allocation.setPortNumber(rs.getInt("port_number"));
        allocation.setUsername(rs.getString("username"));
        allocation.setHostname(rs.getString("hostname"));
        allocation.setStatus(PortAllocation.Status.valueOf(rs.getString("status")));
        
        Timestamp allocatedAt = rs.getTimestamp("allocated_at");
        if (allocatedAt != null) {
            allocation.setAllocatedAt(allocatedAt.toLocalDateTime());
        }
        
        Timestamp lastUsedAt = rs.getTimestamp("last_used_at");
        if (lastUsedAt != null) {
            allocation.setLastUsedAt(lastUsedAt.toLocalDateTime());
        }
        
        allocation.setDescription(rs.getString("description"));
        return allocation;
    }
}
