package com.portpeace.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class PortScanner {
    private static final Logger logger = LoggerFactory.getLogger(PortScanner.class);
    private static final int TIMEOUT_MS = 200;

    public boolean isPortInUse(int port) {
        if (port < 1 || port > 65535) {
            logger.warn("Invalid port number: {}", port);
            return false;
        }

        // Try to bind to the port
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return false; // Port is available
        } catch (IOException e) {
            // Port is in use
            return true;
        }
    }

   
    public boolean isPortInUseByConnect(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), TIMEOUT_MS);
            return true; // Successfully connected, port is in use
        } catch (IOException e) {
            return false; // Connection failed, port is not in use
        }
    }

   
    public int findFirstAvailablePort(int startPort, int endPort) {
        for (int port = startPort; port <= endPort; port++) {
            if (!isPortInUse(port)) {
                return port;
            }
        }
        return -1; // No available port found
    }

   
    public boolean isValidPort(int port) {
        return port >= 1 && port <= 65535;
    }

   
    public boolean isPrivilegedPort(int port) {
        return port >= 1 && port <= 1023;
    }

   
    public String getPortDescription(int port) {
        if (!isValidPort(port)) {
            return "Invalid port number";
        }

        if (isPrivilegedPort(port)) {
            return "Privileged port (requires admin/root access)";
        }

        if (isPortInUse(port)) {
            return "Port is currently in use";
        }

        return "Port is available";
    }
}
