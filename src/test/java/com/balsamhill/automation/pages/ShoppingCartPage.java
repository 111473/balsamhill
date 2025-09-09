package com.balsamhill.automation.pages;

import com.balsamhill.automation.logger.LoggerWrapper;
import com.balsamhill.automation.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ShoppingCartPage {

    private static final LoggerWrapper log = new LoggerWrapper(ShoppingCartPage.class);

    private final WebDriver driver;

    private final By price = By.cssSelector("span[class*='cartProductDetailItem_new_price'] > span");

    public ShoppingCartPage(WebDriver driver){
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver cannot be null");
        }
        this.driver = driver;
    }

    public String getDisplayedProductPrice() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            System.out.println("=== DEBUG: Looking for price in shopping cart ===");
            System.out.println("Current URL: " + driver.getCurrentUrl());
            System.out.println("Page title: " + driver.getTitle());

            // Wait for cart page to load
            Thread.sleep(2000);

            // Check for the original selector
            List<WebElement> originalPriceElements = driver.findElements(By.cssSelector("span[class*='cartProductDetailItem_new_price'] > span"));
            System.out.println("Found " + originalPriceElements.size() + " elements with original selector");

            // Look for various price-related elements
            String[] priceSelectors = {
                    "span[class*='cartProductDetailItem_new_price'] > span",
                    "span[class*='cartProductDetailItem_new_price']",
                    "span[class*='new_price']",
                    "span[class*='price']",
                    ".price",
                    "[class*='price']",
                    ".cart-item-price",
                    ".product-price",
                    "[data-testid*='price']"
            };

            System.out.println("=== Checking different price selectors ===");
            for (String selector : priceSelectors) {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                if (!elements.isEmpty()) {
                    System.out.println("Selector: " + selector + " found " + elements.size() + " elements");
                    for (int i = 0; i < Math.min(3, elements.size()); i++) {
                        WebElement elem = elements.get(i);
                        String text = elem.getText().trim();
                        if (!text.isEmpty()) {
                            System.out.println("  Element " + i + ": text='" + text + "', displayed=" + elem.isDisplayed());
                        }
                    }
                }
            }

            // Look for any elements containing dollar signs or price patterns
            List<WebElement> dollarElements = driver.findElements(By.xpath("//*[contains(text(), '$')]"));
            System.out.println("Found " + dollarElements.size() + " elements containing '$'");

            for (int i = 0; i < Math.min(10, dollarElements.size()); i++) {
                WebElement elem = dollarElements.get(i);
                String text = elem.getText().trim();
                if (text.matches(".*\\$\\d+.*")) { // Contains $ followed by digits
                    System.out.println("Price candidate " + i + ": '" + text + "' (tag: " + elem.getTagName() + ", class: " + elem.getAttribute("class") + ")");
                }
            }

            // Try to find and return a price
            WebElement priceElement = null;

            // First try: original selector
            if (!originalPriceElements.isEmpty() && originalPriceElements.get(0).isDisplayed()) {
                priceElement = originalPriceElements.get(0);
                System.out.println("Using original selector");
            }
            // Second try: other price selectors
            else {
                for (String selector : priceSelectors) {
                    List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                    for (WebElement elem : elements) {
                        if (elem.isDisplayed() && !elem.getText().trim().isEmpty()) {
                            String text = elem.getText().trim();
                            if (text.contains("$") && text.matches(".*\\$\\d+.*")) {
                                priceElement = elem;
                                System.out.println("Using selector: " + selector + " with text: " + text);
                                break;
                            }
                        }
                    }
                    if (priceElement != null) break;
                }
            }

            // Third try: xpath for dollar signs
            if (priceElement == null) {
                for (WebElement elem : dollarElements) {
                    if (elem.isDisplayed()) {
                        String text = elem.getText().trim();
                        if (text.matches(".*\\$\\d+\\.\\d{2}.*")) { // More specific price pattern
                            priceElement = elem;
                            System.out.println("Using dollar element with text: " + text);
                            break;
                        }
                    }
                }
            }

            if (priceElement != null) {
                String priceText = priceElement.getText().trim();
                System.out.println("SUCCESS: Found price: " + priceText);
                return priceText;
            } else {
                throw new RuntimeException("No price element found with any selector");
            }

        } catch (Exception e) {
            System.out.println("ERROR in getDisplayedProductPrice: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve product price from the Shopping Cart Page", e);
        }
    }
}
