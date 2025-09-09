package com.balsamhill.automation.utils;


import com.balsamhill.automation.logger.LoggerWrapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing strings and extracting various data types
 * Common use cases: extracting numbers, counts, prices, percentages from web text
 */
public class StringParserUtils {
    private static final LoggerWrapper log = new LoggerWrapper(StringParserUtils.class);

    private StringParserUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Extracts the first integer from a string
     * Examples: "298 Results" -> 298, "Price: $25.99" -> 25, "No items" -> 0
     */
    public static int extractFirstInteger(String text) {
        return extractFirstInteger(text, 0);
    }

    /**
     * Extracts the first integer from a string with default value
     * @param text The input text
     * @param defaultValue Value to return if no integer is found
     * @return First integer found or default value
     */
    public static int extractFirstInteger(String text, int defaultValue) {
        if (text == null || text.trim().isEmpty()) {
            log.debug("Input text is null or empty, returning default value: {}", defaultValue);
            return defaultValue;
        }

        try {
            // Remove all non-digit characters except minus sign at the beginning
            String numbersOnly = text.replaceAll("[^\\d-]", "");

            // Handle case where we only have a minus sign
            if (numbersOnly.equals("-") || numbersOnly.isEmpty()) {
                log.debug("No valid integer found in text: '{}', returning default: {}", text, defaultValue);
                return defaultValue;
            }

            // Find first number (could be negative)
            Pattern pattern = Pattern.compile("-?\\d+");
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                int result = Integer.parseInt(matcher.group());
                log.debug("Extracted integer {} from text: '{}'", result, text);
                return result;
            }

            log.debug("No integer found in text: '{}', returning default: {}", text, defaultValue);
            return defaultValue;

        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer from text: '{}', returning default: {}", text, defaultValue);
            return defaultValue;
        }
    }
}
