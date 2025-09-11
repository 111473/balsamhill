package com.balsamhill.automation.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.balsamhill.automation.utils.JsonUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Model class to represent search test data
 * Contains all the necessary information for product search and validation tests
 * Enhanced with JSON serialization capabilities and JSR310 support
 */
public class SearchTestData {

    @JsonProperty("searchTerm")
    private String searchTerm;

    @JsonProperty("productIndex")
    private int productIndex;

    @JsonProperty("currentPrice")
    private String currentPrice;

    @JsonProperty("expectedPrice")
    private String expectedPrice;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("productId")
    private String productId;

    @JsonProperty("newPrice")
    private String newPrice;

    @JsonProperty("expectedItemName")
    private String expectedItemName;

    @JsonProperty("customizationOptions")
    private Map<String, String> customizationOptions;

    @JsonProperty("category")
    private String category;

    @JsonProperty("isCustomizable")
    private boolean isCustomizable;

    @JsonProperty("captureTimestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime captureTimestamp;

    @JsonProperty("testEnvironment")
    private String testEnvironment;

    @JsonProperty("browser")
    private String browser;

    // Default constructor
    public SearchTestData() {
        this.captureTimestamp = LocalDateTime.now();
    }

    // Constructor with essential fields
    public SearchTestData(String searchTerm, int productIndex, String currentPrice) {
        this();
        this.searchTerm = searchTerm;
        this.productIndex = productIndex;
        this.currentPrice = currentPrice;
    }

    // Full constructor
    public SearchTestData(String searchTerm, int productIndex, String currentPrice,
                          String expectedPrice, String productName, String productId,
                          Map<String, String> customizationOptions, String category,
                          boolean isCustomizable) {
        this();
        this.searchTerm = searchTerm;
        this.productIndex = productIndex;
        this.currentPrice = currentPrice;
        this.expectedPrice = expectedPrice;
        this.productName = productName;
        this.productId = productId;
        this.customizationOptions = customizationOptions;
        this.category = category;
        this.isCustomizable = isCustomizable;
    }

    // Enhanced JSON Operations
    /**
     * Save this SearchTestData object to JSON file
     * @param filePath Full path where to save the JSON file
     * @throws IOException if file operations fail
     */
    public void saveToJsonFile(String filePath) throws IOException {
        JsonUtils.saveSearchTestDataToJson(this, filePath);
    }

    /**
     * Save with auto-generated filename
     * @param testType Type of test (e.g., "search-results", "product-details")
     * @param browser Browser name
     * @return Full path where file was saved
     * @throws IOException if file operations fail
     */
    public String saveWithAutoFilename(String testType, String browser) throws IOException {
        this.browser = browser;
        return JsonUtils.saveWithAutoFilename(this, testType, browser);
    }

    /**
     * Convert to JSON string
     * @return JSON representation of this object
     * @throws IOException if serialization fails
     */
    public String toJsonString() throws IOException {
        return JsonUtils.toJsonString(this);
    }

    // Getters and Setters
    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public int getProductIndex() {
        return productIndex;
    }

    public void setProductIndex(int productIndex) {
        this.productIndex = productIndex;
    }

    public String getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(String currentPrice) {
        this.currentPrice = currentPrice;
        this.captureTimestamp = LocalDateTime.now(); // Update timestamp when price is captured
    }

    public String getExpectedPrice() {
        return expectedPrice;
    }

    public void setExpectedPrice(String expectedPrice) {
        this.expectedPrice = expectedPrice;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Map<String, String> getCustomizationOptions() {
        return customizationOptions;
    }

    public void setCustomizationOptions(Map<String, String> customizationOptions) {
        this.customizationOptions = customizationOptions;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isCustomizable() {
        return isCustomizable;
    }

    public void setCustomizable(boolean customizable) {
        isCustomizable = customizable;
    }

    public String getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(String newPrice) {
        this.newPrice = newPrice;
    }

    public String getExpectedItemName() {
        return expectedItemName;
    }

    public void setExpectedItemName(String expectedItemName) {
        this.expectedItemName = expectedItemName;
    }

    public LocalDateTime getCaptureTimestamp() {
        return captureTimestamp;
    }

    public void setCaptureTimestamp(LocalDateTime captureTimestamp) {
        this.captureTimestamp = captureTimestamp;
    }

    public String getTestEnvironment() {
        return testEnvironment;
    }

    public void setTestEnvironment(String testEnvironment) {
        this.testEnvironment = testEnvironment;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    // Utility methods
    public boolean hasCustomizationOptions() {
        return customizationOptions != null && !customizationOptions.isEmpty();
    }

    public String getFormattedPrice() {
        if (currentPrice != null && !currentPrice.startsWith("$")) {
            return "$" + currentPrice;
        }
        return currentPrice;
    }

    public String getFormattedNewPrice() {
        if (newPrice != null && !newPrice.startsWith("$")) {
            return "$" + newPrice;
        }
        return newPrice;
    }

    public boolean isValidTestData() {
        return searchTerm != null && !searchTerm.trim().isEmpty()
                && productIndex >= 0
                && currentPrice != null && !currentPrice.trim().isEmpty();
    }

    /**
     * Check if price capture is recent (within last hour)
     * @return true if captured within last hour
     */
    public boolean isRecentCapture() {
        if (captureTimestamp == null) {
            return false;
        }
        return captureTimestamp.isAfter(LocalDateTime.now().minusHours(1));
    }

    /**
     * Get formatted capture info
     * @return Formatted string with capture details
     */
    public String getCaptureInfo() {
        return String.format("Captured at: %s, Browser: %s, Environment: %s",
                captureTimestamp != null ? captureTimestamp.toString() : "Unknown",
                browser != null ? browser : "Unknown",
                testEnvironment != null ? testEnvironment : "Unknown");
    }

    @Override
    public String toString() {
        return "SearchTestData{" +
                "searchTerm='" + searchTerm + '\'' +
                ", productIndex=" + productIndex +
                ", currentPrice='" + currentPrice + '\'' +
                ", expectedPrice='" + expectedPrice + '\'' +
                ", newPrice='" + newPrice + '\'' +
                ", expectedItemName='" + expectedItemName + '\'' +
                ", customizationOptions=" + customizationOptions +
                ", captureTimestamp=" + captureTimestamp +
                ", browser='" + browser + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchTestData that = (SearchTestData) o;
        return productIndex == that.productIndex &&
                isCustomizable == that.isCustomizable &&
                Objects.equals(searchTerm, that.searchTerm) &&
                Objects.equals(currentPrice, that.currentPrice) &&
                Objects.equals(expectedPrice, that.expectedPrice) &&
                Objects.equals(productName, that.productName) &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(newPrice, that.newPrice) &&
                Objects.equals(expectedItemName, that.expectedItemName) &&
                Objects.equals(customizationOptions, that.customizationOptions) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchTerm, productIndex, currentPrice, expectedPrice,
                productName, productId, newPrice, expectedItemName,
                customizationOptions, category, isCustomizable);
    }

    // Enhanced Builder pattern for easier object creation
    public static class Builder {
        private SearchTestData searchTestData;

        public Builder() {
            searchTestData = new SearchTestData();
        }

        public Builder searchTerm(String searchTerm) {
            searchTestData.setSearchTerm(searchTerm);
            return this;
        }

        public Builder productIndex(int productIndex) {
            searchTestData.setProductIndex(productIndex);
            return this;
        }

        public Builder currentPrice(String currentPrice) {
            searchTestData.setCurrentPrice(currentPrice);
            return this;
        }

        public Builder expectedPrice(String expectedPrice) {
            searchTestData.setExpectedPrice(expectedPrice);
            return this;
        }

        public Builder productName(String productName) {
            searchTestData.setProductName(productName);
            return this;
        }

        public Builder productId(String productId) {
            searchTestData.setProductId(productId);
            return this;
        }

        public Builder newPrice(String newPrice) {
            searchTestData.setNewPrice(newPrice);
            return this;
        }

        public Builder expectedItemName(String expectedItemName) {
            searchTestData.setExpectedItemName(expectedItemName);
            return this;
        }

        public Builder customizationOptions(Map<String, String> customizationOptions) {
            searchTestData.setCustomizationOptions(customizationOptions);
            return this;
        }

        public Builder category(String category) {
            searchTestData.setCategory(category);
            return this;
        }

        public Builder isCustomizable(boolean isCustomizable) {
            searchTestData.setCustomizable(isCustomizable);
            return this;
        }

        public Builder testEnvironment(String testEnvironment) {
            searchTestData.setTestEnvironment(testEnvironment);
            return this;
        }

        public Builder browser(String browser) {
            searchTestData.setBrowser(browser);
            return this;
        }

        public Builder captureTimestamp(LocalDateTime timestamp) {
            searchTestData.setCaptureTimestamp(timestamp);
            return this;
        }

        /**
         * Build and save to JSON with auto-generated filename
         * @param testType Type of test
         * @param browser Browser name
         * @return Built SearchTestData object
         * @throws IOException if JSON save fails
         */
        public SearchTestData buildAndSaveToJson(String testType, String browser) throws IOException {
            SearchTestData data = build();
            data.saveWithAutoFilename(testType, browser);
            return data;
        }

        /**
         * Build and save to specific JSON file path
         * @param filePath Full path to save JSON file
         * @return Built SearchTestData object
         * @throws IOException if JSON save fails
         */
        public SearchTestData buildAndSaveToJsonFile(String filePath) throws IOException {
            SearchTestData data = build();
            data.saveToJsonFile(filePath);
            return data;
        }

        public SearchTestData build() {
            return searchTestData;
        }
    }
}