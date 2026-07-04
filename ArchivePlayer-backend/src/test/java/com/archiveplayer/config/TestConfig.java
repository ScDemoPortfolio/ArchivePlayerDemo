package com.archiveplayer.config;

/**
 * Centralized configuration for UI Tests.
 * Allows switching environments via System Properties.
 */
public class TestConfig {
    public static final String BASE_URL = System.getProperty("test.url", "http://localhost:5173");
    public static final boolean HEADLESS = Boolean.parseBoolean(System.getProperty("test.headless", "true"));
    public static final int DEFAULT_WAIT = Integer.parseInt(System.getProperty("test.wait", "10"));

    public static boolean isLocal() {
        return BASE_URL.contains("localhost");
    }
}