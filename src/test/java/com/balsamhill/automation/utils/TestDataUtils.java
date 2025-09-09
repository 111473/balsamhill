package com.balsamhill.automation.utils;

import com.balsamhill.automation.models.SearchTestData;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for managing test data
 * Provides data providers for TestNG tests and handles JSON test data loading/updating
 */
public class TestDataUtils {

    private static final String TEST_DATA_FILE = "src/test/resources/testdata.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * DataProvider for search tests
     * Loads test data from JSON file and provides it to test methods
     */
    @DataProvider(name = "searchDataProvider")
    public static Object[][] searchDataProvider() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(TEST_DATA_FILE));
            List<SearchTestData> testDataList = mapper.convertValue(
                    rootNode.get("tests"),
                    new TypeReference<List<SearchTestData>>() {}
            );

            Object[][] data = new Object[testDataList.size()][1];
            for (int i = 0; i < testDataList.size(); i++) {
                data[i][0] = testDataList.get(i);
            }
            return data;

        } catch (IOException e) {
            e.printStackTrace();
            // Return fallback test data if JSON loading fails
            return getFallbackTestData();
        }
    }

    /**
     * Update a specific property in the JSON file for a given test index.
     */
    public static void updateProperty(int index, String fieldName, String newValue) {
        try {
            File file = new File(TEST_DATA_FILE);

            JsonNode rootNode = mapper.readTree(file);

            // Ensure we're working with an array under "tests"
            JsonNode testsNode = rootNode.get("tests");
            if (testsNode == null || !testsNode.isArray()) {
                throw new IllegalStateException("Invalid JSON structure: 'tests' array not found.");
            }

            // Get the object at index
            JsonNode testNode = testsNode.get(index);
            if (testNode == null || !testNode.isObject()) {
                throw new IllegalStateException("Invalid index: " + index + " not found in 'tests' array.");
            }

            // Cast to ObjectNode to allow mutation
            ObjectNode testObject = (ObjectNode) testNode;
            testObject.put(fieldName, newValue);

            // Write back to file (overwrite)
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);

        } catch (IOException e) {
            throw new RuntimeException("Error updating test data file", e);
        }
    }

    /**
     * Convenience wrapper to update current price.
     */
    public static void updateCurrentPrice(int index, String newPrice) {
        updateProperty(index, "currentPrice", newPrice);
    }

    /**
     * Update new price for a specific test
     */
    public static void updateNewPrice(int testIndex, String newPrice) {
        updateProperty(testIndex, "newPrice", newPrice);
    }

    /**
     * Update expected item name for a specific test
     */
    public static void updateExpectedItemName(int testIndex, String expectedItemName) {
        updateProperty(testIndex, "expectedItemName", expectedItemName);
    }

    /**
     * Get all test data from JSON file
     */
    public static List<SearchTestData> getAllTestData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(new File(TEST_DATA_FILE));
        return mapper.convertValue(
                rootNode.get("tests"),
                new TypeReference<List<SearchTestData>>() {}
        );
    }

    /**
     * Get test data by index
     */
    public static SearchTestData getTestDataByIndex(int index) {
        try {
            List<SearchTestData> testDataList = getAllTestData();
            if (index >= 0 && index < testDataList.size()) {
                return testDataList.get(index);
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get test data by search term
     */
    public static SearchTestData getTestDataBySearchTerm(String searchTerm) {
        try {
            List<SearchTestData> testDataList = getAllTestData();
            return testDataList.stream()
                    .filter(data -> searchTerm.equals(data.getSearchTerm()))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update customization options for a specific test
     */
    public static void updateCustomizationOptions(int index, Map<String, String> customizationOptions) {
        try {
            File file = new File(TEST_DATA_FILE);
            JsonNode rootNode = mapper.readTree(file);
            JsonNode testsNode = rootNode.get("tests");

            if (testsNode == null || !testsNode.isArray()) {
                throw new IllegalStateException("Invalid JSON structure: 'tests' array not found.");
            }

            JsonNode testNode = testsNode.get(index);
            if (testNode == null || !testNode.isObject()) {
                throw new IllegalStateException("Invalid index: " + index + " not found in 'tests' array.");
            }

            ObjectNode testObject = (ObjectNode) testNode;
            ObjectNode customizationNode = mapper.createObjectNode();

            // Convert Map to ObjectNode
            customizationOptions.forEach(customizationNode::put);
            testObject.set("customizationOptions", customizationNode);

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);

        } catch (IOException e) {
            throw new RuntimeException("Error updating customization options in test data file", e);
        }
    }

    /**
     * Add a new test data entry to the JSON file
     */
    public static void addTestData(SearchTestData newTestData) {
        try {
            File file = new File(TEST_DATA_FILE);
            JsonNode rootNode = mapper.readTree(file);
            JsonNode testsNode = rootNode.get("tests");

            if (testsNode == null || !testsNode.isArray()) {
                throw new IllegalStateException("Invalid JSON structure: 'tests' array not found.");
            }

            // Convert SearchTestData to JsonNode
            JsonNode newTestNode = mapper.valueToTree(newTestData);

            // Add to the array
            ((com.fasterxml.jackson.databind.node.ArrayNode) testsNode).add(newTestNode);

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);

        } catch (IOException e) {
            throw new RuntimeException("Error adding new test data to file", e);
        }
    }

    /**
     * Get fallback test data when JSON file is not available or fails to load
     */
    private static Object[][] getFallbackTestData() {
        SearchTestData testData1 = createFallbackTestData1();
        SearchTestData testData2 = createFallbackTestData2();
        SearchTestData testData3 = createFallbackTestData3();

        return new Object[][] {
                { testData1 },
                { testData2 },
                { testData3 }
        };
    }

    private static SearchTestData createFallbackTestData1() {
        Map<String, String> customizations = new HashMap<>();
        customizations.put("size", "7.5ft");
        customizations.put("lightType", "Clear");

        return new SearchTestData.Builder()
                .searchTerm("Christmas Tree")
                .productIndex(0)
                .currentPrice("299.99")
                .newPrice("299.99")
                .expectedItemName("Classic Christmas Tree")
                .customizationOptions(customizations)
                .build();
    }

    private static SearchTestData createFallbackTestData2() {
        return new SearchTestData.Builder()
                .searchTerm("Garland")
                .productIndex(1)
                .currentPrice("89.99")
                .newPrice("89.99")
                .expectedItemName("Classic Garland")
                .build();
    }

    private static SearchTestData createFallbackTestData3() {
        Map<String, String> customizations = new HashMap<>();
        customizations.put("diameter", "24 inch");
        customizations.put("ribbon", "Red Velvet");

        return new SearchTestData.Builder()
                .searchTerm("Wreath")
                .productIndex(0)
                .currentPrice("129.99")
                .newPrice("129.99")
                .expectedItemName("Holiday Wreath")
                .customizationOptions(customizations)
                .build();
    }

    /**
     * Validate test data file structure
     */
    public static boolean validateTestDataFile() {
        try {
            File file = new File(TEST_DATA_FILE);
            if (!file.exists()) {
                return false;
            }

            JsonNode rootNode = mapper.readTree(file);
            JsonNode testsNode = rootNode.get("tests");

            return testsNode != null && testsNode.isArray();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Create a sample test data file if it doesn't exist
     */
    public static void createSampleTestDataFile() throws IOException {
        File file = new File(TEST_DATA_FILE);

        if (!file.exists()) {
            // Create directory if it doesn't exist
            file.getParentFile().mkdirs();

            // Create sample JSON structure
            ObjectNode rootNode = mapper.createObjectNode();
            com.fasterxml.jackson.databind.node.ArrayNode testsArray = mapper.createArrayNode();

            // Add sample test data
            testsArray.add(mapper.valueToTree(createFallbackTestData1()));
            testsArray.add(mapper.valueToTree(createFallbackTestData2()));
            testsArray.add(mapper.valueToTree(createFallbackTestData3()));

            rootNode.set("tests", testsArray);

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, rootNode);
            System.out.println("Sample test data file created at: " + TEST_DATA_FILE);
        }
    }
}