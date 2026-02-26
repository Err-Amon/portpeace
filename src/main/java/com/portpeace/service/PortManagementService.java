package com.portpeace.service;

import com.portpeace.model.PortAllocation;
import com.portpeace.repository.PortAllocationRepository;
import com.portpeace.util.PortScanner;
import com.portpeace.util.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;


public class PortManagementService {
    private static final Logger logger = LoggerFactory.getLogger(PortManagementService.class);
    
    private final PortAllocationRepository repository;
    private final PortScanner portScanner;
    private final int defaultPortRangeStart = 3000;
    private final int defaultPortRangeEnd = 9999;

    public PortManagementService() {
        this.repository = new PortAllocationRepository();
        this.portScanner = new PortScanner();
    }

    public AllocationResult allocatePort(String serviceName, Integer preferredPort, String description) {
        String username = SystemUtils.getUsername();
        String hostname = SystemUtils.getHostname();

        // Check if service already has an allocation
        Optional<PortAllocation> existing = repository.findByServiceName(serviceName);
        if (existing.isPresent()) {
            PortAllocation allocation = existing.get();
            
            // If port is still free, reuse it
            if (!portScanner.isPortInUse(allocation.getPortNumber())) {
                allocation.setStatus(PortAllocation.Status.ACTIVE);
                repository.updateStatus(serviceName, PortAllocation.Status.ACTIVE);
                
                logger.info("Reusing existing port {} for service {}", allocation.getPortNumber(), serviceName);
                return AllocationResult.success(allocation, "Reused existing allocation");
            } else {
                // Port is in use, need to find a new one
                logger.warn("Port {} for service {} is now in use, allocating new port", 
                           allocation.getPortNumber(), serviceName);
            }
        }

        // Find an available port
        int port;
        if (preferredPort != null) {
            // Check if preferred port is available
            if (portScanner.isPortInUse(preferredPort)) {
                Optional<PortAllocation> conflicting = repository.findByPortNumber(preferredPort);
                if (conflicting.isPresent()) {
                    String message = String.format("Port %d is already allocated to service '%s'",
                            preferredPort, conflicting.get().getServiceName());
                    return AllocationResult.failure(message);
                }
                return AllocationResult.failure("Port " + preferredPort + " is already in use");
            }
            port = preferredPort;
        } else {
            // Find next available port
            Optional<Integer> availablePort = findAvailablePort(defaultPortRangeStart, defaultPortRangeEnd);
            if (availablePort.isEmpty()) {
                return AllocationResult.failure("No available ports in range " + 
                        defaultPortRangeStart + "-" + defaultPortRangeEnd);
            }
            port = availablePort.get();
        }

        // Create allocation
        PortAllocation allocation = new PortAllocation(
                serviceName, port, username, hostname, PortAllocation.Status.ACTIVE);
        allocation.setDescription(description);

        if (repository.save(allocation)) {
            repository.logHistory(serviceName, port, username, hostname, "ALLOCATED", 
                    "Port allocated successfully");
            logger.info("Allocated port {} for service {}", port, serviceName);
            return AllocationResult.success(allocation, "Port allocated successfully");
        } else {
            return AllocationResult.failure("Failed to save allocation to database");
        }
    }

   
    public boolean freePort(String serviceName) {
        Optional<PortAllocation> existing = repository.findByServiceName(serviceName);
        
        if (existing.isEmpty()) {
            logger.warn("No allocation found for service: {}", serviceName);
            return false;
        }

        PortAllocation allocation = existing.get();
        String username = SystemUtils.getUsername();
        String hostname = SystemUtils.getHostname();

        if (repository.deleteByServiceName(serviceName)) {
            repository.logHistory(serviceName, allocation.getPortNumber(), 
                    username, hostname, "FREED", "Port freed by user");
            logger.info("Freed port {} from service {}", allocation.getPortNumber(), serviceName);
            return true;
        }

        return false;
    }

   
    public List<PortAllocation> listAllocations() {
        return repository.findAll();
    }

 
    public List<PortAllocation> listActiveAllocations() {
        return repository.findByStatus(PortAllocation.Status.ACTIVE);
    }

   
    public Optional<PortAllocation> getServiceAllocation(String serviceName) {
        return repository.findByServiceName(serviceName);
    }

    
    public PortStatus checkPortStatus(int port) {
        boolean inUse = portScanner.isPortInUse(port);
        Optional<PortAllocation> allocation = repository.findByPortNumber(port);
        
        return new PortStatus(port, inUse, allocation.orElse(null));
    }

  
    public int cleanupInactiveAllocations(int daysOld) {
        return repository.cleanupInactive(daysOld);
    }

    
    private Optional<Integer> findAvailablePort(int start, int end) {
        for (int port = start; port <= end; port++) {
            if (!portScanner.isPortInUse(port) && repository.findByPortNumber(port).isEmpty()) {
                return Optional.of(port);
            }
        }
        return Optional.empty();
    }

   
    public static class AllocationResult {
        private final boolean success;
        private final PortAllocation allocation;
        private final String message;

        private AllocationResult(boolean success, PortAllocation allocation, String message) {
            this.success = success;
            this.allocation = allocation;
            this.message = message;
        }

        public static AllocationResult success(PortAllocation allocation, String message) {
            return new AllocationResult(true, allocation, message);
        }

        public static AllocationResult failure(String message) {
            return new AllocationResult(false, null, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public PortAllocation getAllocation() {
            return allocation;
        }

        public String getMessage() {
            return message;
        }
    }

    
    public static class PortStatus {
        private final int port;
        private final boolean inUse;
        private final PortAllocation allocation;

        public PortStatus(int port, boolean inUse, PortAllocation allocation) {
            this.port = port;
            this.inUse = inUse;
            this.allocation = allocation;
        }

        public int getPort() {
            return port;
        }

        public boolean isInUse() {
            return inUse;
        }

        public PortAllocation getAllocation() {
            return allocation;
        }
    }
}
