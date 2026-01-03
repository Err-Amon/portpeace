package com.portpeace.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * System utility methods
 */
public class SystemUtils {
    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);
    
    private static String cachedUsername;
    private static String cachedHostname;

    /**
     * Get current system username
     */
    public static String getUsername() {
        if (cachedUsername == null) {
            cachedUsername = System.getProperty("user.name", "unknown");
            logger.debug("Retrieved username: {}", cachedUsername);
        }
        return cachedUsername;
    }

    /**
     * Get current hostname
     */
    public static String getHostname() {
        if (cachedHostname == null) {
            try {
                cachedHostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                logger.warn("Failed to get hostname, using 'localhost'", e);
                cachedHostname = "localhost";
            }
            logger.debug("Retrieved hostname: {}", cachedHostname);
        }
        return cachedHostname;
    }

    /**
     * Get operating system name
     */
    public static String getOSName() {
        return System.getProperty("os.name", "Unknown");
    }

    /**
     * Get operating system version
     */
    public static String getOSVersion() {
        return System.getProperty("os.version", "Unknown");
    }

    /**
     * Check if running on Windows
     */
    public static boolean isWindows() {
        return getOSName().toLowerCase().contains("windows");
    }

    /**
     * Check if running on macOS
     */
    public static boolean isMacOS() {
        String os = getOSName().toLowerCase();
        return os.contains("mac") || os.contains("darwin");
    }

    /**
     * Check if running on Linux
     */
    public static boolean isLinux() {
        return getOSName().toLowerCase().contains("linux");
    }

    /**
     * Get Java version
     */
    public static String getJavaVersion() {
        return System.getProperty("java.version", "Unknown");
    }

    /**
     * Get user home directory
     */
    public static String getUserHome() {
        return System.getProperty("user.home", ".");
    }

    /**
     * Get system information summary
     */
    public static String getSystemInfo() {
        return String.format("User: %s, Host: %s, OS: %s %s, Java: %s",
                getUsername(), getHostname(), getOSName(), getOSVersion(), getJavaVersion());
    }
}
