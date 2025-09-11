package com.balsamhill.automation.pages;

import com.balsamhill.automation.logger.LoggerWrapper;
import com.balsamhill.automation.utils.WaitUtils;
import com.balsamhill.automation.utils.WebElementUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchResultsPage {

    private static final LoggerWrapper log = new LoggerWrapper(SearchResultsPage.class);

    private final WebDriver driver;

    private final By items = By.cssSelector("div[class*='col-md-4']");

    /**
     * Constructor initializes the WebDriver instance.
     */
    public SearchResultsPage(WebDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver cannot be null");
        }
        this.driver = driver;
    }

    /**
     * Select product by index
     */
    public String selectProductAndGetPrice(int testIndex) {
        List<WebElement> products = getInitialItems(items);
        log.step("Initial number of products found: {}", products.size());

        int productIndex = testIndex - 1;
        WebElement selectedProduct = products.get(productIndex);

        String currentPrice = extractPrice(selectedProduct.getText());

        WebElementUtils.click(selectedProduct);
        WaitUtils.waitForPageLoad();

        log.step("Navigated to product page of item at productIndex {}", productIndex + 1);

        return currentPrice;
    }

    /**
     * Extracts the most relevant price from the product details text.
     * Logic:
     * - If "Save" is present, return the second price if available, otherwise the first.
     * - If "Free Shipping" is present, return the last price.
     * - Otherwise, return the last price.
     *
     * @param details The text containing product details and prices
     * @return The extracted price as a String, or null if no prices found
     */
    private String extractPrice(String details) {
        if (details == null || details.trim().isEmpty()) {
            return null;
        }
        log.step("Extracting price from details: {}", details);

        // Find all prices using a simple, robust regex
        List<String> prices = findAllPrices(details);
        log.step("Prices extracted: {}", prices);

        if (prices.isEmpty()) {
            log.warn("No prices found!");
            return null;
        }
        log.step("Prices found: {}", prices);

        String lowerDetails = details.toLowerCase();
        boolean hasSave = lowerDetails.contains("save");
        boolean hasFreeShipping = lowerDetails.contains("free shipping") || lowerDetails.contains("free-shipping");

        String result;
        if (hasSave) {
            result = prices.size() >= 2 ? prices.get(1) : prices.get(0);
            log.step("Logic: Save detected -> returning 2nd price (or 1st if only one): " + result);
        } else if (hasFreeShipping) {
            result = prices.get(prices.size() - 1);
            log.step("Logic: Free shipping detected -> returning last price: " + result);
        } else {
            result = prices.get(prices.size() - 1);
            log.step("Logic: No keywords -> returning last price: " + result);
        }
        return result;
    }

    /**
     * Finds all prices in the text using a robust regex
     */
    private static List<String> findAllPrices(String text) {
        List<String> prices = new ArrayList<>();

        // This regex handles: $1,199 $999 $1,999.99 $99.99 etc.
        Pattern pattern = Pattern.compile("\\$[0-9,]+(\\.[0-9]{2})?");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String price = matcher.group();
            prices.add(price);
            log.step("Found price: {}", price);
        }

        return prices;
    }

    /**
     * Collect items from the page
     */
    private static List<WebElement> getInitialItems(By items) {
        List<WebElement> allItems = new ArrayList<>();

        try {
            List<WebElement> currentItems = WaitUtils.findElementsWithWait(items);

            for (WebElement item : currentItems) {
                allItems.add(item);
            }

            log.step("Collected {} items total", allItems.size());

        } catch (Exception e) {
            log.error("Error during item collection: {}", e.getMessage());
        }

        return allItems;
    }
}