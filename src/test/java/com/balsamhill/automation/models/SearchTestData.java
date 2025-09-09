package com.balsamhill.automation.models;

import java.util.Map;
import java.util.Objects;

/**
 * Model class to represent search test data
 * Contains all the necessary information for product search and validation tests
 */
public class SearchTestData {

    private String searchTerm;
    private int productIndex;
    private String currentPrice;
    private String expectedPrice;
    private String productName;
    private String productId;
    private String newPrice;
    private String expectedItemName;
    private Map<String, String> customizationOptions;
    private String category;
    private boolean isCustomizable;

    // Default constructor
    public SearchTestData() {}

    // Constructor with essential fields
    public SearchTestData(String searchTerm, int productIndex, String currentPrice) {
        this.searchTerm = searchTerm;
        this.productIndex = productIndex;
        this.currentPrice = currentPrice;
    }

    // Full constructor
    public SearchTestData(String searchTerm, int productIndex, String currentPrice,
                          String expectedPrice, String productName, String productId,
                          Map<String, String> customizationOptions, String category,
                          boolean isCustomizable) {
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

    @Override
    public String toString() {
        return "SearchTestData{" +
                "searchTerm='" + searchTerm + '\'' +
                ", productIndex=" + productIndex +
                ", customizationOptions=" + customizationOptions +
                ", currentPrice='" + currentPrice + '\'' +
                ", newPrice='" + newPrice + '\'' +
                ", expectedItemName='" + expectedItemName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchTestData that = (SearchTestData) o;
        return productIndex == that.productIndex &&
                Objects.equals(searchTerm, that.searchTerm) &&
                Objects.equals(customizationOptions, that.customizationOptions) &&
                Objects.equals(currentPrice, that.currentPrice) &&
                Objects.equals(newPrice, that.newPrice) &&
                Objects.equals(expectedItemName, that.expectedItemName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchTerm, productIndex, customizationOptions,
                currentPrice, newPrice, expectedItemName);
    }

    // Builder pattern for easier object creation
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

        public Builder customizationOptions(Map<String, String> customizationOptions) {
            searchTestData.setCustomizationOptions(customizationOptions);
            return this;
        }


        public Builder currentPrice(String currentPrice) {
            searchTestData.setCurrentPrice(currentPrice);
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

        public SearchTestData build() {
            return searchTestData;
        }


    }
}