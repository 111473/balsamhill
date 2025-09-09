package com.balsamhill.automation.utils;

import java.io.InputStream;
import com.balsamhill.automation.logger.LoggerWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigManager {

    private static final LoggerWrapper log = new LoggerWrapper(ConfigManager.class);
    private static final JsonNode rootNode;

    static {
        try (InputStream inputStream = ConfigManager.class.getClassLoader().getResourceAsStream("config.json")) {
            if (inputStream == null) {
                throw new RuntimeException("config.json not found in classpath.");
            }
            ObjectMapper mapper = new ObjectMapper();
            rootNode = mapper.readTree(inputStream);
            log.info("[CONFIG] config.json loaded successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.json", e);
        }
    }

    /**
     * Get a config value as String
     */
    public static String get(String key) {
        JsonNode valueNode = rootNode.path(key);
        if (valueNode.isMissingNode()) {
            log.warn("[CONFIG] Key '{}' not found in config.json", key);
            return null;
        }
        String value = valueNode.asText();
        log.info("[CONFIG] Retrieved key '{}' = '{}'", key, value);
        return value;
    }

    /**
     * Get a config value as boolean
     */
    public static boolean getBoolean(String key) {
        JsonNode valueNode = rootNode.path(key);
        if (valueNode.isMissingNode()) {
            log.warn("[CONFIG] Key '{}' not found in config.json", key);
            return false;
        }
        boolean value = valueNode.asBoolean();
        log.info("[CONFIG] Retrieved key '{}' = '{}'", key, value);
        return value;
    }
}
