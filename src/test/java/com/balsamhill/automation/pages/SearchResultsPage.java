package com.balsamhill.automation.pages;

import com.balsamhill.automation.logger.LoggerWrapper;
import com.balsamhill.automation.models.SearchTestData;
import com.balsamhill.automation.utils.TestDataUtils;
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
    private final By price = By.cssSelector("a[aria-label*='Alpine Balsam Fir'] span[class*='productCard_prod-sale-price']");

    private static String currentPrice;

    /**
     * Constructor initializes the WebDriver instance.
     */
    public SearchResultsPage(WebDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver cannot be null");
        }
        this.driver = driver;
    }

    // Temporary solution
    public void selectProduct(int testIndex) {
        List<WebElement> products = getInitialItems(items);
        log.step("Initial number of products found: {}", products.size());

        // Use productIndex from testData (3 in your JSON → 4th item, 0-based array)
        int productIndex = testIndex - 1;
        WebElement selectedProduct = products.get(productIndex);

        WebElementUtils.click(selectedProduct);
        WaitUtils.waitForPageLoad();

        log.step("Navigated to product page of item at productIndex {}", productIndex + 1);
    }


    /**
     * Select a product based on 1-based index and navigate to the product page.
     *
     * @param testIndex Index of the test case in the JSON data
     * @return ProductPage object
     */
    public void selectProductAndStorePrice(int testIndex, SearchTestData testData) {
        List<WebElement> products = getInitialItems(items);
        log.step("Initial number of products found: {}", products.size());

        // Use productIndex from testData (3 in your JSON → 4th item, 0-based array)
        int productIndex = testData.getProductIndex() - 1;
        WebElement selectedProduct = products.get(productIndex);

        setTestData(testData, testIndex, selectedProduct);

        WebElementUtils.click(selectedProduct);
        WaitUtils.waitForPageLoad();

        log.step("Navigated to product page of item at productIndex {}", productIndex + 1);
    }

    public void setTestData(SearchTestData testData, int testIndex, WebElement elem) {
        String extractedPrice = extractPrice(elem.getText());
        log.step("Extracted price of product: {}", extractedPrice);

        // Update POJO in memory
        testData.setCurrentPrice(extractedPrice);
        log.info("Updated POJO with current price: {}", extractedPrice);

        // Update JSON on disk
        TestDataUtils.updateCurrentPrice(testIndex, extractedPrice);
        log.info("Updated JSON file at test index {}", testIndex);
    }


    public static String fetchBasePriceFromJson(String currentPrice) {

        return currentPrice;
    }

    public String getDisplayedProductPrice() {
        try {
            WebElement priceElement = WaitUtils.waitForElementPresent(price);
            String priceText = priceElement.getText().trim();
            log.step("Product price found on Search Result Page: {}", priceText);
            return priceText;
        } catch (Exception e) {
            log.error("Failed to retrieve product price from the Search Results Page: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve product price from the Search Results Page.", e);
        }
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
    public static String extractPrice(String details) {
        if (details == null || details.trim().isEmpty()) {
            return null;
        }
        log.step("Extracting price from details: {}", details);

        // Find all prices using a simple, robust regex
        List<String> prices = findAllPrices(details);
        log.step("Prices extracted: {}", prices);

        if (prices.isEmpty()) {
            System.out.println("No prices found!");
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
     * Collects initial items on the page and scrolling "Show More" as needed.
     *
     * @param items          By locator for the items to collect
     * @return List of all collected WebElements
     */
    private static List<WebElement> getInitialItems(By items) {
        List<WebElement> allItems = new ArrayList<>();

        try {
            while (true) {
                // Use the new WaitUtils method
                List<WebElement> currentItems = WaitUtils.findElementsWithWait(items);

                for (WebElement item : currentItems) {
                    allItems.add(item);
                }
                log.step("Collected {} items so far", allItems.size());
                break;
            }
        } catch (Exception e) {
            log.error("Error during item collection: {}", e.getMessage());
        }

        return allItems;
    }

}
