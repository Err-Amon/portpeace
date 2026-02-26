package com.portpeace.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class SystemUtils {
    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);
    
    private static String cachedUsername;
    private static String cachedHostname;

   
    public static String getUsername() {
        if (cachedUsername == null) {
            cachedUsername = System.getProperty("user.name", "unknown");
            logger.debug("Retrieved username: {}", cachedUsername);
        }
        return cachedUsername;
    }

    
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

   
    public static String getOSName() {
        return System.getProperty("os.name", "Unknown");
    }

   
    public static String getOSVersion() {
        return System.getProperty("os.version", "Unknown");
    }

   
    public static boolean isWindows() {
        return getOSName().toLowerCase().contains("windows");
    }

  
    public static boolean isMacOS() {
        String os = getOSName().toLowerCase();
        return os.contains("mac") || os.contains("darwin");
    }

    
    public static boolean isLinux() {
        return getOSName().toLowerCase().contains("linux");
    }

    
    public static String getJavaVersion() {
        return System.getProperty("java.version", "Unknown");
    }

   
    public static String getUserHome() {
        return System.getProperty("user.home", ".");
    }

    public static String getSystemInfo() {
        return String.format("User: %s, Host: %s, OS: %s %s, Java: %s",
                getUsername(), getHostname(), getOSName(), getOSVersion(), getJavaVersion());
    }
}
