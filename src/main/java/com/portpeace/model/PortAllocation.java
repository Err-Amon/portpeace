package com.portpeace.model;

import java.time.LocalDateTime;


public class PortAllocation {
    
    public enum Status {
        ACTIVE, INACTIVE, RESERVED
    }

    private int id;
    private String serviceName;
    private int portNumber;
    private String username;
    private String hostname;
    private Status status;
    private LocalDateTime allocatedAt;
    private LocalDateTime lastUsedAt;
    private String description;

    public PortAllocation() {
    }

    public PortAllocation(String serviceName, int portNumber, String username, 
                         String hostname, Status status) {
        this.serviceName = serviceName;
        this.portNumber = portNumber;
        this.username = username;
        this.hostname = hostname;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getAllocatedAt() {
        return allocatedAt;
    }

    public void setAllocatedAt(LocalDateTime allocatedAt) {
        this.allocatedAt = allocatedAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("PortAllocation{service='%s', port=%d, status=%s, user=%s@%s}",
                serviceName, portNumber, status, username, hostname);
    }
}
