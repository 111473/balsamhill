package com.balsamhill.automation.pages;

import com.balsamhill.automation.drivers.DriverManager;
import com.balsamhill.automation.logger.LoggerWrapper;
import com.balsamhill.automation.utils.WaitUtils;
import com.balsamhill.automation.utils.WebElementUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class ShoppingCartPage {

    private static final LoggerWrapper log = new LoggerWrapper(ShoppingCartPage.class);

    private final WebDriver driver;

    private final By originalPriceSelector = By.cssSelector("span[class*='cartProductDetailItem_new_price'] > span");
    private final By one = By.cssSelector("#productQuantityInput_0_desktop");
    private final By keyWord = By.cssSelector("div.cartProductDetailItem_product-name-wrapper__2Yaco a > span");

    private final String[] priceSelectors = {
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

    public ShoppingCartPage(WebDriver driver){
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver cannot be null");
        }
        this.driver = driver;
    }

    public String getDisplayedProductPrice() {
        try {
            waitForCartPageToLoad();

            // Strategy pattern for different price finding approaches
            PriceFindingStrategy[] strategies = {
                    this::findPriceWithOriginalSelector,
                    this::findPriceWithConfiguredSelectors,
                    this::findPriceWithXPathSearch,
                    this::findPriceWithFallbackPattern
            };

            for (PriceFindingStrategy strategy : strategies) {
                String price = strategy.findPrice();
                if (price != null) {
                    log.info("Found price using strategy: {}", strategy.getClass().getSimpleName());
                    return price;
                }
            }

            throw new RuntimeException("No price element found with any available strategy");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Price retrieval was interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve product price from cart page", e);
        }
    }

    public void deleteItem() {
        List<By> deleteSelectors = getDeleteSelectors();
        By loaderSelector = By.cssSelector(".bLoader_loader-wrapper-box__q4A7u");

        waitForPageReadiness(loaderSelector);

        for (By selector : deleteSelectors) {
            if (attemptDeleteWithSelector(selector, loaderSelector)) {
                log.info("Successfully deleted item using selector: {}", selector.toString());
                return;
            }
        }

        throw new RuntimeException("Failed to delete item - no clickable delete button found");
    }

    public boolean isCartIconItemCountDisplayed(){

        return WaitUtils.isDisplayed(one);
    }

    public boolean isKeyWordHasBeenRemovedDisplayed() {
        return WaitUtils.isDisplayed(keyWord);
    }


    @FunctionalInterface
    private interface PriceFindingStrategy {
        String findPrice();
    }

    private void waitForCartPageToLoad() throws InterruptedException {
        // Use WaitUtils instead of manual WebDriverWait
        WaitUtils.waitForPageLoad(15);

        // Brief wait for dynamic content - consider replacing with specific element wait
        Thread.sleep(2000);
    }

    private String findPriceWithOriginalSelector() {
        try {
            List<WebElement> elements = WaitUtils.findElementsWithWait(originalPriceSelector, 3);

            for (WebElement element : elements) {
                if (WaitUtils.isDisplayed(originalPriceSelector, 1)) {
                    String priceText = element.getText().trim();
                    if (isValidPrice(priceText)) {
                        log.debug("Found price with original selector: {}", priceText);
                        return priceText;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Original selector failed: {}", e.getMessage());
        }
        return null;
    }

    private String findPriceWithConfiguredSelectors() {
        for (String selectorString : priceSelectors) {
            try {
                By selector = By.cssSelector(selectorString);
                List<WebElement> elements = WaitUtils.findElementsWithWait(selector, 2);

                if (log.isDebugEnabled()) {
                    debugLogElements(elements, selectorString);
                }

                for (WebElement element : elements) {
                    if (element.isDisplayed()) {
                        String priceText = element.getText().trim();
                        if (isValidPrice(priceText)) {
                            log.debug("Found price with selector '{}': {}", selectorString, priceText);
                            return priceText;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Selector '{}' failed: {}", selectorString, e.getMessage());
            }
        }
        return null;
    }

    private String findPriceWithXPathSearch() {
        try {
            By dollarXPath = By.xpath("//*[contains(text(), '$')]");
            List<WebElement> dollarElements = WaitUtils.findElementsWithWait(dollarXPath, 3);

            if (log.isDebugEnabled()) {
                debugLogPriceCandidates(dollarElements);
            }

            // Look for elements with proper price format
            for (WebElement element : dollarElements) {
                if (element.isDisplayed()) {
                    String text = element.getText().trim();
                    if (isValidPriceFormat(text)) {
                        log.debug("Found price with XPath search: {}", text);
                        return text;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("XPath search failed: {}", e.getMessage());
        }
        return null;
    }

    private String findPriceWithFallbackPattern() {
        try {
            // More comprehensive XPath for price patterns
            By fallbackXPath = By.xpath("//*[matches(text(), '\\$\\d+\\.\\d{2}')]");
            List<WebElement> elements = WaitUtils.findElementsWithWait(fallbackXPath, 2);

            for (WebElement element : elements) {
                if (element.isDisplayed()) {
                    String text = element.getText().trim();
                    if (isValidPriceFormat(text)) {
                        log.debug("Found price with fallback pattern: {}", text);
                        return text;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Fallback pattern search failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Validates if the text contains a valid price
     */
    private boolean isValidPrice(String text) {
        return text != null && !text.isEmpty() && text.contains("$") && text.matches(".*\\$\\d+.*");
    }

    /**
     * Validates if the text matches a proper price format ($XX.XX)
     */
    private boolean isValidPriceFormat(String text) {
        return text != null && text.matches(".*\\$\\d+\\.\\d{2}.*");
    }

    /**
     * Debug logging for elements found with selectors
     */
    private void debugLogElements(List<WebElement> elements, String selector) {
        if (elements.isEmpty()) {
            log.debug("No elements found for selector: {}", selector);
            return;
        }

        log.debug("Found {} elements for selector: {}", elements.size(), selector);

        int elementsToLog = Math.min(3, elements.size());
        for (int i = 0; i < elementsToLog; i++) {
            WebElement element = elements.get(i);
            String text = element.getText().trim();
            if (!text.isEmpty()) {
                log.debug("  Element {}: text='{}', displayed={}", i, text, element.isDisplayed());
            }
        }
    }

    /**
     * Debug logging for price candidate elements
     */
    private void debugLogPriceCandidates(List<WebElement> dollarElements) {
        if (dollarElements.isEmpty()) {
            log.debug("No elements containing '$' found");
            return;
        }

        log.debug("Found {} elements containing '$'", dollarElements.size());

        int candidatesToLog = Math.min(10, dollarElements.size());
        for (int i = 0; i < candidatesToLog; i++) {
            WebElement element = dollarElements.get(i);
            String text = element.getText().trim();
            if (text.matches(".*\\$\\d+.*")) {
                String className = element.getAttribute("class");
                log.debug("Price candidate {}: '{}' (tag: {}, class: {})",
                        i, text, element.getTagName(), className);
            }
        }
    }

    private List<By> getDeleteSelectors() {
        return Arrays.asList(
                By.cssSelector(".delete.cartProductDetailItem_delete-icon__8MHyf"),
                By.cssSelector("[data-testid='delete-button']"),
                By.cssSelector(".delete-icon"),
                By.cssSelector("button[title*='delete'], button[title*='remove']"),
                By.xpath("//button[contains(@class,'delete')]//ancestor-or-self::*[contains(@class,'icon')]"),
                By.xpath("//button[@aria-label[contains(.,'delete') or contains(.,'remove')]]")
        );
    }

    private void waitForPageReadiness(By loaderSelector) {
        WaitUtils.waitForLoaderToDisappear(loaderSelector);

        // Wait for animations and page load
        try {
            Thread.sleep(1000); // Brief wait for animations
            WaitUtils.waitForPageLoad(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Page stabilization interrupted");
        }
    }

    private boolean attemptDeleteWithSelector(By selector, By loaderSelector) {
        try {
            if (!WaitUtils.isElementPresent(selector, 2)) {
                log.debug("Element not found: {}", selector.toString());
                return false;
            }

            WebElement deleteElement = WaitUtils.waitForElementToBeClickable(selector, 10);

            // Optional: Check if element is actually clickable (not obscured)
            if (!WaitUtils.isElementActuallyClickable(deleteElement)) {
                log.debug("Element not clickable (obscured): {}", selector.toString());
                return false;
            }

            WebElementUtils.scrollToElement(deleteElement);
            return executeClickStrategies(deleteElement, loaderSelector);

        } catch (Exception e) {
            log.debug("Selector failed {}: {}", selector.toString(), e.getMessage());
            return false;
        }
    }

    private boolean executeClickStrategies(WebElement element, By loaderSelector) {
        ClickStrategy[] strategies = {
                () -> tryDirectClick(element),
                () -> tryJavaScriptClick(element),
                () -> tryActionsClick(element),
                () -> tryClickWithOffset(element),
                () -> tryClickParentButton(element)
        };

        for (ClickStrategy strategy : strategies) {
            if (strategy.execute()) {
                return waitForDeleteAction(loaderSelector);
            }
        }
        return false;
    }

    @FunctionalInterface
    private interface ClickStrategy {
        boolean execute();
    }

    private boolean tryDirectClick(WebElement element) {
        try {
            element.click();
            log.debug("Direct click successful");
            return true;
        } catch (Exception e) {
            log.debug("Direct click failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean tryJavaScriptClick(WebElement element) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
            js.executeScript("arguments[0].click();", element);
            log.debug("JavaScript click successful");
            return true;
        } catch (Exception e) {
            log.debug("JavaScript click failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean tryActionsClick(WebElement element) {
        try {
            Actions actions = new Actions(DriverManager.getDriver());
            actions.moveToElement(element)
                    .pause(Duration.ofMillis(300))
                    .click()
                    .perform();
            log.debug("Actions click successful");
            return true;
        } catch (Exception e) {
            log.debug("Actions click failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean tryClickWithOffset(WebElement element) {
        try {
            Actions actions = new Actions(DriverManager.getDriver());
            Dimension size = element.getSize();
            int centerX = size.getWidth() / 2;
            int centerY = size.getHeight() / 2;

            actions.moveToElement(element, centerX, centerY)
                    .pause(Duration.ofMillis(300))
                    .click()
                    .perform();
            log.debug("Offset click successful");
            return true;
        } catch (Exception e) {
            log.debug("Offset click failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean tryClickParentButton(WebElement element) {
        try {
            WebElement parentButton = element.findElement(By.xpath("./ancestor::button[1]"));
            parentButton.click();
            log.debug("Parent button click successful");
            return true;
        } catch (NoSuchElementException e) {
            log.debug("No parent button found");
            return false;
        } catch (Exception e) {
            log.debug("Parent button click failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean waitForDeleteAction(By loaderSelector) {
        try {
            // Use WaitUtils for loader detection and waiting
            WaitUtils.isElementPresent(loaderSelector, 2); // Just check, don't fail if not found
            WaitUtils.waitForLoaderToDisappear(loaderSelector, 15);

            Thread.sleep(1000); // Allow UI to update
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Delete action verification interrupted");
            return false;
        }
    }

}
