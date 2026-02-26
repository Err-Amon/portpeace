package com.portpeace.cli;

import com.portpeace.model.PortAllocation;
import com.portpeace.service.PortManagementService;
import com.portpeace.util.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


public class CommandLineInterface {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineInterface.class);
    private final PortManagementService service;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CommandLineInterface(PortManagementService service) {
        this.service = service;
    }

   
    public void processCommand(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String command = args[0].toLowerCase();

        try {
            switch (command) {
    case "alloc":
    case "allocate":
        handleAllocate(args);
        break;
    case "free":
    case "release":
        handleFree(args);
        break;
    case "list":
    case "ls":
        handleList(args);
        break;
    case "status":
    case "stat":
        handleStatus(args);
        break;
    case "cleanup":
        handleCleanup(args);
        break;
    case "help":
    case "--help":
    case "-h":
        printUsage();
        break;
    case "version":
    case "--version":
    case "-v":
        printVersion();
        break;
    default:
        System.err.println("Unknown command: " + command);
        printUsage();
        break;
}

        } catch (Exception e) {
            logger.error("Error executing command: {}", command, e);
            System.err.println("Error: " + e.getMessage());
        }
    }

    
    private void handleAllocate(String[] args) {
        if (args.length < 2) {
            System.err.println("Error: Service name is required");
            System.out.println("Usage: portpeace alloc <service-name> [port] [description]");
            return;
        }

        String serviceName = args[1];
        Integer preferredPort = null;
        String description = null;

        // Parse optional port number
        if (args.length >= 3) {
            try {
                preferredPort = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                // If not a number, treat as description
                description = args[2];
            }
        }

        // Parse optional description
        if (args.length >= 4) {
            description = args[3];
        }

        PortManagementService.AllocationResult result = 
                service.allocatePort(serviceName, preferredPort, description);

        if (result.isSuccess()) {
            PortAllocation allocation = result.getAllocation();
            System.out.println("✓ Port allocated successfully!");
            System.out.println();
            System.out.println("  Service:     " + allocation.getServiceName());
            System.out.println("  Port:        " + allocation.getPortNumber());
            System.out.println("  URL:         http://localhost:" + allocation.getPortNumber());
            System.out.println("  Local URL:   http://" + allocation.getServiceName() + ".dev:" + allocation.getPortNumber());
            System.out.println("  Status:      " + allocation.getStatus());
            
            if (allocation.getDescription() != null) {
                System.out.println("  Description: " + allocation.getDescription());
            }
            
            System.out.println();
            System.out.println("Start your service on port " + allocation.getPortNumber());
        } else {
            System.err.println("✗ Failed to allocate port: " + result.getMessage());
        }
    }

    private void handleFree(String[] args) {
        if (args.length < 2) {
            System.err.println("Error: Service name is required");
            System.out.println("Usage: portpeace free <service-name>");
            return;
        }

        String serviceName = args[1];
        
        if (service.freePort(serviceName)) {
            System.out.println("✓ Port freed successfully for service: " + serviceName);
        } else {
            System.err.println("✗ Failed to free port. Service not found: " + serviceName);
        }
    }

   
    private void handleList(String[] args) {
        List<PortAllocation> allocations = service.listAllocations();

        if (allocations.isEmpty()) {
            System.out.println("No port allocations found.");
            return;
        }

        System.out.println("Current Port Allocations:");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.printf("%-20s %-8s %-10s %-15s %-20s%n", 
                "SERVICE", "PORT", "STATUS", "USER", "LAST USED");
        System.out.println("───────────────────────────────────────────────────────────────────────");

        for (PortAllocation allocation : allocations) {
            String lastUsed = allocation.getLastUsedAt() != null 
                    ? allocation.getLastUsedAt().format(DATE_FORMATTER) 
                    : "N/A";
            
            System.out.printf("%-20s %-8d %-10s %-15s %-20s%n",
                    truncate(allocation.getServiceName(), 20),
                    allocation.getPortNumber(),
                    allocation.getStatus(),
                    truncate(allocation.getUsername(), 15),
                    lastUsed);
        }

        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("Total: " + allocations.size() + " allocation(s)");
    }

    private void handleStatus(String[] args) {
        if (args.length < 2) {
            // Show system status
            showSystemStatus();
        } else {
            // Show specific service or port status
            String target = args[1];
            
            try {
                int port = Integer.parseInt(target);
                showPortStatus(port);
            } catch (NumberFormatException e) {
                showServiceStatus(target);
            }
        }
    }

   
    private void showSystemStatus() {
        System.out.println("PortPeace System Status");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("System Information:");
        System.out.println("  " + SystemUtils.getSystemInfo());
        System.out.println();
        
        List<PortAllocation> active = service.listActiveAllocations();
        System.out.println("Active Allocations: " + active.size());
        
        List<PortAllocation> all = service.listAllocations();
        System.out.println("Total Allocations:  " + all.size());
        System.out.println("═══════════════════════════════════════════════════════════════════════");
    }

  
    private void showPortStatus(int port) {
        PortManagementService.PortStatus status = service.checkPortStatus(port);
        
        System.out.println("Port " + port + " Status");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("  In Use:      " + (status.isInUse() ? "Yes" : "No"));
        
        if (status.getAllocation() != null) {
            PortAllocation allocation = status.getAllocation();
            System.out.println("  Allocated:   Yes");
            System.out.println("  Service:     " + allocation.getServiceName());
            System.out.println("  User:        " + allocation.getUsername() + "@" + allocation.getHostname());
            System.out.println("  Status:      " + allocation.getStatus());
            System.out.println("  Allocated:   " + allocation.getAllocatedAt().format(DATE_FORMATTER));
        } else {
            System.out.println("  Allocated:   No");
        }
        System.out.println("═══════════════════════════════════════════════════════════════════════");
    }

    private void showServiceStatus(String serviceName) {
        Optional<PortAllocation> allocation = service.getServiceAllocation(serviceName);
        
        if (allocation.isEmpty()) {
            System.out.println("No allocation found for service: " + serviceName);
            return;
        }

        PortAllocation alloc = allocation.get();
        System.out.println("Service: " + serviceName);
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("  Port:        " + alloc.getPortNumber());
        System.out.println("  Status:      " + alloc.getStatus());
        System.out.println("  User:        " + alloc.getUsername() + "@" + alloc.getHostname());
        System.out.println("  Allocated:   " + alloc.getAllocatedAt().format(DATE_FORMATTER));
        System.out.println("  Last Used:   " + alloc.getLastUsedAt().format(DATE_FORMATTER));
        
        if (alloc.getDescription() != null) {
            System.out.println("  Description: " + alloc.getDescription());
        }
        
        boolean inUse = service.checkPortStatus(alloc.getPortNumber()).isInUse();
        System.out.println("  Port In Use: " + (inUse ? "Yes" : "No"));
        System.out.println("═══════════════════════════════════════════════════════════════════════");
    }

    
    private void handleCleanup(String[] args) {
        int daysOld = 7; // Default cleanup age
        
        if (args.length >= 2) {
            try {
                daysOld = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid number of days: " + args[1]);
                return;
            }
        }

        int cleaned = service.cleanupInactiveAllocations(daysOld);
        System.out.println("✓ Cleaned up " + cleaned + " inactive allocation(s) older than " + daysOld + " days");
    }

    
    private void printUsage() {
        System.out.println("PortPeace - Professional Port Management Tool");
        System.out.println();
        System.out.println("USAGE:");
        System.out.println("  portpeace <command> [options]");
        System.out.println();
        System.out.println("COMMANDS:");
        System.out.println("  alloc <service> [port] [desc]  Allocate a port for a service");
        System.out.println("  free <service>                  Free a port allocation");
        System.out.println("  list                            List all port allocations");
        System.out.println("  status [service|port]           Show system or specific status");
        System.out.println("  cleanup [days]                  Clean up old allocations (default: 7 days)");
        System.out.println("  help                            Show this help message");
        System.out.println("  version                         Show version information");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  portpeace alloc frontend");
        System.out.println("  portpeace alloc backend 4000");
        System.out.println("  portpeace alloc api 5000 \"REST API Service\"");
        System.out.println("  portpeace free frontend");
        System.out.println("  portpeace list");
        System.out.println("  portpeace status frontend");
        System.out.println("  portpeace status 3000");
    }

    
    private void printVersion() {
        System.out.println("PortPeace v1.0.0");
        System.out.println("Professional Port Management Tool");
        System.out.println("Copyright (c) 2025");
    }

  
    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}
