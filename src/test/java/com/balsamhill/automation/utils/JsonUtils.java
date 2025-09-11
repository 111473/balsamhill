package com.balsamhill.automation.utils;

import com.balsamhill.automation.models.SearchTestData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Add this import

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

/**
 * Utility class for JSON operations related to test data
 * Provides methods for saving, loading, and managing SearchTestData in JSON format
 */
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEFAULT_OUTPUT_DIR = "test-results/json-data/";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

    static {
        // Register JSR310 module for Java 8 date/time support
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // Rest of your existing methods remain the same...

    /**
     * Save SearchTestData to JSON file
     * @param data SearchTestData object to save
     * @param filePath Full path where to save the JSON file
     * @throws IOException if file operations fail
     */
    public static void saveSearchTestDataToJson(SearchTestData data, String filePath) throws IOException {
        ensureDirectoryExists(filePath);
        objectMapper.writeValue(new File(filePath), data);
    }

    /**
     * Load SearchTestData from JSON file
     * @param filePath Path to the JSON file
     * @return SearchTestData object
     * @throws IOException if file operations fail
     */
    public static SearchTestData loadSearchTestDataFromJson(String filePath) throws IOException {
        return objectMapper.readValue(new File(filePath), SearchTestData.class);
    }

    /**
     * Save multiple SearchTestData objects to JSON array file
     * @param dataList List of SearchTestData objects
     * @param filePath Full path where to save the JSON file
     * @throws IOException if file operations fail
     */
    public static void saveSearchTestDataListToJson(List<SearchTestData> dataList, String filePath) throws IOException {
        ensureDirectoryExists(filePath);
        objectMapper.writeValue(new File(filePath), dataList);
    }

    /**
     * Load multiple SearchTestData objects from JSON array file
     * @param filePath Path to the JSON file
     * @return List of SearchTestData objects
     * @throws IOException if file operations fail
     */
    public static List<SearchTestData> loadSearchTestDataListFromJson(String filePath) throws IOException {
        return objectMapper.readValue(new File(filePath), new TypeReference<List<SearchTestData>>(){});
    }

    /**
     * Generate timestamped filename for test data
     * @param testType Type of test (e.g., "search-results", "product-details")
     * @param browser Browser name
     * @param searchTerm Search term used in test
     * @return Generated filename
     */
    public static String generateTimestampedFilename(String testType, String browser, String searchTerm) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String cleanSearchTerm = searchTerm.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        return String.format("%s_%s_%s_%s.json", testType, browser, cleanSearchTerm, timestamp);
    }

    /**
     * Generate full file path with default directory
     * @param filename Filename to use
     * @return Full file path
     */
    public static String generateFullPath(String filename) {
        return DEFAULT_OUTPUT_DIR + filename;
    }

    /**
     * Save test data with auto-generated filename
     * @param data SearchTestData to save
     * @param testType Type of test
     * @param browser Browser name
     * @return Full path where file was saved
     * @throws IOException if file operations fail
     */
    public static String saveWithAutoFilename(SearchTestData data, String testType, String browser) throws IOException {
        String filename = generateTimestampedFilename(testType, browser, data.getSearchTerm());
        String fullPath = generateFullPath(filename);
        saveSearchTestDataToJson(data, fullPath);
        return fullPath;
    }

    /**
     * Append test data to existing JSON array file, or create new if doesn't exist
     * @param data SearchTestData to append
     * @param filePath Path to the JSON array file
     * @throws IOException if file operations fail
     */
    public static void appendToJsonArray(SearchTestData data, String filePath) throws IOException {
        List<SearchTestData> dataList;

        File file = new File(filePath);
        if (file.exists()) {
            dataList = loadSearchTestDataListFromJson(filePath);
        } else {
            dataList = new ArrayList<>();
        }

        dataList.add(data);
        saveSearchTestDataListToJson(dataList, filePath);
    }

    /**
     * Create a summary report from multiple test data files
     * @param directoryPath Directory containing JSON files
     * @return Summary as formatted string
     * @throws IOException if file operations fail
     */
    public static String createSummaryReport(String directoryPath) throws IOException {
        StringBuilder summary = new StringBuilder();
        summary.append("=== Test Data Summary Report ===\n");
        summary.append("Generated at: ").append(LocalDateTime.now()).append("\n\n");

        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            return "Directory not found: " + directoryPath;
        }

        File[] jsonFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));
        if (jsonFiles == null || jsonFiles.length == 0) {
            return "No JSON files found in directory: " + directoryPath;
        }

        int totalFiles = 0;
        int successfulParsing = 0;

        for (File file : jsonFiles) {
            totalFiles++;
            try {
                SearchTestData data = loadSearchTestDataFromJson(file.getAbsolutePath());
                summary.append(String.format("File: %s\n", file.getName()));
                summary.append(String.format("  Search Term: %s\n", data.getSearchTerm()));
                summary.append(String.format("  Current Price: %s\n", data.getCurrentPrice()));
                summary.append(String.format("  Product Index: %d\n", data.getProductIndex()));
                summary.append(String.format("  Valid Test Data: %s\n\n", data.isValidTestData()));
                successfulParsing++;
            } catch (Exception e) {
                summary.append(String.format("File: %s - ERROR: %s\n\n", file.getName(), e.getMessage()));
            }
        }

        summary.append(String.format("Summary: %d/%d files processed successfully\n", successfulParsing, totalFiles));
        return summary.toString();
    }

    /**
     * Validate JSON file structure
     * @param filePath Path to JSON file
     * @return true if valid, false otherwise
     */
    public static boolean validateJsonFile(String filePath) {
        try {
            SearchTestData data = loadSearchTestDataFromJson(filePath);
            return data.isValidTestData();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clean old JSON files based on age
     * @param directoryPath Directory to clean
     * @param daysOld Files older than this many days will be deleted
     * @return Number of files deleted
     */
    public static int cleanOldJsonFiles(String directoryPath, int daysOld) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            return 0;
        }

        long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60L * 60L * 1000L);
        int deletedCount = 0;

        File[] jsonFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));
        if (jsonFiles != null) {
            for (File file : jsonFiles) {
                if (file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++;
                    }
                }
            }
        }

        return deletedCount;
    }

    /**
     * Convert SearchTestData to formatted JSON string
     * @param data SearchTestData object
     * @return Formatted JSON string
     * @throws IOException if serialization fails
     */
    public static String toJsonString(SearchTestData data) throws IOException {
        return objectMapper.writeValueAsString(data);
    }

//    /**
//     * Create directory if it doesn't exist
//     * @param filePath Full file path
//     * @throws IOException if directory creation fails
//     */
//    private static void ensureDirectoryExists(String filePath) throws IOException {
//        Path path = Paths.get(filePath).getParent();
//        if (path != null && !Files.exists(path)) {
//            Files.createDirectories(path);
//        }
//    }

    /**
     * Create directory if it doesn't exist - ENHANCED WITH DEBUGGING
     * @param filePath Full file path
     * @throws IOException if directory creation fails
     */
    private static void ensureDirectoryExists(String filePath) throws IOException {
        Path path = Paths.get(filePath).getParent();
        if (path != null) {
            System.out.println("JsonUtils - Ensuring directory exists: " + path.toString());
            System.out.println("JsonUtils - Directory exists before creation: " + Files.exists(path));

            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path);
                    System.out.println("JsonUtils - Directory creation successful: " + Files.exists(path));
                } catch (Exception e) {
                    System.out.println("JsonUtils - Directory creation failed: " + e.getMessage());
                    throw e;
                }
            }

            // Check permissions
            System.out.println("JsonUtils - Directory is writable: " + Files.isWritable(path));
        } else {
            System.out.println("JsonUtils - No parent directory found for path: " + filePath);
        }
    }
}