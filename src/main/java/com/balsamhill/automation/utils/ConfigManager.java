package com.balsamhill.automation.utils;

import com.balsamhill.automation.logger.LoggerWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private static final LoggerWrapper log = new LoggerWrapper(ConfigManager.class);
    private static JsonNode config;
    private static final String[] CONFIG_PATHS = {
            "config.json",                          // From classpath
            "src/test/resources/config.json",       // Maven structure
            "test/resources/config.json",           // Alternative
            "resources/config.json",                // Alternative
            "config/config.json"                    // Alternative
    };

    // Fallback configuration if file not found
    private static final String DEFAULT_CONFIG = "{\n" +
            "  \"browser\": [\"chrome\"],\n" +
            "  \"baseUrl\": \"https://www.balsamhill.com/login\",\n" +
            "  \"headless\": false,\n" +
            "  \"timeout\": {\n" +
            "    \"implicit\": 10,\n" +
            "    \"explicit\": 30,\n" +
            "    \"pageLoad\": 60\n" +
            "  },\n" +
            "  \"retry\": {\n" +
            "    \"maxAttempts\": 3,\n" +
            "    \"enabled\": true\n" +
            "  },\n" +
            "  \"screenshot\": {\n" +
            "    \"onFailure\": true,\n" +
            "    \"onSuccess\": false,\n" +
            "    \"path\": \"target/screenshots\"\n" +
            "  }\n" +
            "}";

    static {
        loadConfig();
    }

    /**
     * Load configuration from JSON file with multiple fallback strategies
     */
    private static void loadConfig() {
        ObjectMapper mapper = new ObjectMapper();

        // Strategy 1: Try to load from classpath
        try {
            InputStream configStream = ConfigManager.class.getClassLoader()
                    .getResourceAsStream("config.json");

            if (configStream != null) {
                config = mapper.readTree(configStream);
                log.info("Configuration loaded successfully from classpath: config.json");
                return;
            }
        } catch (IOException e) {
            log.warn("Failed to load config from classpath: {}", e.getMessage());
        }

        // Strategy 2: Try multiple file paths
        for (String path : CONFIG_PATHS) {
            try {
                File configFile = new File(path);
                if (configFile.exists() && configFile.canRead()) {
                    config = mapper.readTree(configFile);
                    log.info("Configuration loaded successfully from: {}", path);
                    return;
                }
            } catch (IOException e) {
                log.debug("Failed to load config from {}: {}", path, e.getMessage());
            }
        }

        // Strategy 3: Use default configuration
        try {
            config = mapper.readTree(DEFAULT_CONFIG);
            log.warn("Configuration file not found in any location. Using default configuration.");
            log.info("Searched locations: {}", String.join(", ", CONFIG_PATHS));
            log.info("Please create config.json in src/test/resources/ for custom configuration.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default configuration", e);
        }
    }

    /**
     * Get string value from configuration
     * For cross-browser testing, if browser is an array, returns the first browser
     */
    public static String get(String key) {
        if (config == null) {
            loadConfig();
        }

        JsonNode node = config.get(key);
        if (node == null) {
            log.warn("Configuration key not found: {}. Using empty string.", key);
            return "";
        }

        // Handle array values (like browsers) by returning first element
        if (node.isArray() && node.size() > 0) {
            return node.get(0).asText();
        }

        return node.asText();
    }

    /**
     * Get nested property using dot notation (e.g., "timeout.implicit")
     */
    public static String getNestedProperty(String path) {
        if (config == null) {
            loadConfig();
        }

        String[] parts = path.split("\\.");
        JsonNode current = config;

        for (String part : parts) {
            current = current.get(part);
            if (current == null) {
                log.warn("Configuration path not found: {}. Using empty string.", path);
                return "";
            }
        }

        if (current.isArray() && current.size() > 0) {
            return current.get(0).asText();
        }

        return current.asText();
    }

    /**
     * Get nested property with default value
     */
    public static String getNestedProperty(String path, String defaultValue) {
        String value = getNestedProperty(path);
        if (value.isEmpty()) {
            log.debug("Using default value for '{}': {}", path, defaultValue);
            return defaultValue;
        }
        return value;
    }

    /**
     * Get nested integer value
     */
    public static int getNestedInt(String path, int defaultValue) {
        try {
            String value = getNestedProperty(path);
            if (value.isEmpty()) {
                return defaultValue;
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid integer value for '{}', using default: {}", path, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Get nested boolean value
     */
    public static boolean getNestedBoolean(String path, boolean defaultValue) {
        try {
            String value = getNestedProperty(path);
            if (value.isEmpty()) {
                return defaultValue;
            }
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            log.warn("Invalid boolean value for '{}', using default: {}", path, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Get boolean value from configuration
     */
    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * Get boolean value with default
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        if (config == null) {
            loadConfig();
        }

        JsonNode node = config.get(key);
        if (node == null) {
            log.warn("Configuration key '{}' not found, using default: {}", key, defaultValue);
            return defaultValue;
        }

        return node.asBoolean();
    }

    /**
     * Get integer value from configuration
     */
    public static int getInt(String key, int defaultValue) {
        if (config == null) {
            loadConfig();
        }

        JsonNode node = config.get(key);
        if (node == null) {
            log.warn("Configuration key '{}' not found, using default: {}", key, defaultValue);
            return defaultValue;
        }

        return node.asInt();
    }

    /**
     * Get list of browsers for cross-browser testing
     */
    public static List<String> getBrowsers() {
        if (config == null) {
            loadConfig();
        }

        List<String> browsers = new ArrayList<>();
        JsonNode browserNode = config.get("browser");

        if (browserNode == null) {
            log.warn("Browser configuration not found, using default: chrome");
            browsers.add("chrome");
            return browsers;
        }

        if (browserNode.isArray()) {
            // Multiple browsers configured
            for (JsonNode browser : browserNode) {
                browsers.add(browser.asText());
            }
        } else {
            // Single browser configured
            browsers.add(browserNode.asText());
        }

        log.info("Configured browsers: {}", browsers);
        return browsers;
    }

    /**
     * Get value with default fallback
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        if (value.isEmpty()) {
            log.debug("Using default value for '{}': {}", key, defaultValue);
            return defaultValue;
        }
        return value;
    }

    /**
     * Check if configuration has a specific key
     */
    public static boolean hasKey(String key) {
        if (config == null) {
            loadConfig();
        }
        return config.has(key);
    }

    /**
     * Reload configuration from file
     */
    public static void reloadConfig() {
        log.info("Reloading configuration...");
        config = null;
        loadConfig();
    }

    // Convenience methods for common configurations

    public static int getImplicitTimeout() {
        return getNestedInt("timeout.implicit", 10);
    }

    public static int getExplicitTimeout() {
        return getNestedInt("timeout.explicit", 30);
    }

    public static int getPageLoadTimeout() {
        return getNestedInt("timeout.pageLoad", 60);
    }

    public static int getMaxRetryAttempts() {
        return getNestedInt("retry.maxAttempts", 3);
    }

    public static boolean isRetryEnabled() {
        return getNestedBoolean("retry.enabled", true);
    }

    public static boolean isScreenshotOnFailure() {
        return getNestedBoolean("screenshot.onFailure", true);
    }

    public static String getScreenshotPath() {
        return getNestedProperty("screenshot.path", "target/screenshots");
    }

    public static String getUsername() {
        String username = System.getenv("BALSAM_USERNAME");
        if (username == null || username.isEmpty()) {
            log.warn("Username not found in environment variable: BALSAM_USERNAME");
            return "";
        }
        return username;
    }

    public static String getPassword() {
        String password = System.getenv("BALSAM_PASSWORD");
        if (password == null || password.isEmpty()) {
            log.warn("Password not found in environment variable: BALSAM_PASSWORD");
            return "";
        }
        return password;
    }

    /**
     * Debug method to print current configuration
     */
    public static void printConfigInfo() {
        log.info("=== Configuration Debug Info ===");
        log.info("Config loaded: {}", config != null);
        if (config != null) {
            log.info("Available keys: {}", config.fieldNames());
        }

        log.info("Working directory: {}", System.getProperty("user.dir"));
        log.info("Searched paths:");
        for (String path : CONFIG_PATHS) {
            File file = new File(path);
            log.info("  {} - exists: {}, readable: {}", path, file.exists(), file.canRead());
        }
        log.info("===============================");
    }
}