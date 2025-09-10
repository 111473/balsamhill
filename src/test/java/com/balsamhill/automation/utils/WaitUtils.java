package com.balsamhill.automation.utils;

import com.balsamhill.automation.drivers.DriverManager;
import com.balsamhill.automation.logger.LoggerWrapper;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class WaitUtils {
    private static final LoggerWrapper log = new LoggerWrapper(WaitUtils.class);
    private static final int DEFAULT_TIMEOUT = 10;
    private static final int SHORT_TIMEOUT = 5;
    private static final int LOADER_DETECTION_TIMEOUT = 2;

    private WaitUtils() {
        // Private constructor to prevent instantiation
    }

    private static WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    private static WebDriverWait getWait(int timeoutSeconds) {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSeconds));
    }

    private static WebDriverWait getWait() {
        return getWait(DEFAULT_TIMEOUT);
    }

    // Element visibility waits
    public static WebElement waitForElementToBeVisible(By locator) {
        return waitForElementToBeVisible(locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForElementToBeVisible(By locator, int timeoutSeconds) {
        try {
            WebElement element = getWait(timeoutSeconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
            log.debug("Element became visible: {}", locator);
            return element;
        } catch (Exception e) {
            log.error("Element not visible within {} seconds: {}", timeoutSeconds, locator);
            throw new RuntimeException("Element not visible: " + locator, e);
        }
    }

    // Element clickability waits
    public static WebElement waitForElementToBeClickable(By locator) {
        return waitForElementToBeClickable(locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForElementToBeClickable(By locator, int timeoutSeconds) {
        try {
            WebElement element = getWait(timeoutSeconds).until(ExpectedConditions.elementToBeClickable(locator));
            log.debug("Element became clickable: {}", locator);
            return element;
        } catch (Exception e) {
            log.error("Element not clickable within {} seconds: {}", timeoutSeconds, locator);
            throw new RuntimeException("Element not clickable: " + locator, e);
        }
    }

    public static WebElement waitForElementToBeClickable(WebElement element) {
        return waitForElementToBeClickable(element, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForElementToBeClickable(WebElement element, int timeoutSeconds) {
        try {
            WebElement clickableElement = getWait(timeoutSeconds).until(ExpectedConditions.elementToBeClickable(element));
            log.debug("WebElement became clickable");
            return clickableElement;
        } catch (Exception e) {
            log.error("WebElement not clickable within {} seconds", timeoutSeconds);
            throw new RuntimeException("WebElement not clickable", e);
        }
    }

    // Element presence waits
    public static WebElement waitForElementPresent(By locator) {
        return waitForElementPresent(locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForElementPresent(By locator, int timeoutSeconds) {
        try {
            WebElement element = getWait(timeoutSeconds).until(ExpectedConditions.presenceOfElementLocated(locator));
            log.debug("Element present: {}", locator);
            return element;
        } catch (Exception e) {
            log.error("Element not present within {} seconds: {}", timeoutSeconds, locator);
            throw new RuntimeException("Element not present: " + locator, e);
        }
    }

    // Multiple elements wait
    public static List<WebElement> waitForAllElementsVisible(By locator) {
        return waitForAllElementsVisible(locator, DEFAULT_TIMEOUT);
    }

    public static List<WebElement> waitForAllElementsVisible(By locator, int timeoutSeconds) {
        try {
            List<WebElement> elements = getWait(timeoutSeconds).until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
            log.debug("All elements visible: {} (count: {})", locator, elements.size());
            return elements;
        } catch (Exception e) {
            log.error("Not all elements visible within {} seconds: {}", timeoutSeconds, locator);
            throw new RuntimeException("Not all elements visible: " + locator, e);
        }
    }

    // Find elements with wait (returns empty list on failure)
    public static List<WebElement> findElementsWithWait(By locator) {
        return findElementsWithWait(locator, SHORT_TIMEOUT);
    }

    public static List<WebElement> findElementsWithWait(By locator, int timeoutSeconds) {
        try {
            getWait(timeoutSeconds).until(ExpectedConditions.presenceOfElementLocated(locator));
            List<WebElement> elements = getDriver().findElements(locator);
            log.debug("Found {} elements with wait for locator: {}", elements.size(), locator);
            return elements;
        } catch (Exception e) {
            log.debug("No elements found within {} seconds for locator: {}", timeoutSeconds, locator);
            return new ArrayList<>();
        }
    }

    // Page state waits
    public static boolean waitForUrlContains(String urlFraction) {
        return waitForUrlContains(urlFraction, DEFAULT_TIMEOUT);
    }

    public static boolean waitForUrlContains(String urlFraction, int timeoutSeconds) {
        try {
            boolean result = getWait(timeoutSeconds).until(ExpectedConditions.urlContains(urlFraction));
            log.debug("URL contains '{}': {}", urlFraction, result);
            return result;
        } catch (Exception e) {
            log.error("URL does not contain '{}' within {} seconds", urlFraction, timeoutSeconds);
            return false;
        }
    }

    public static boolean waitForTitleContains(String title) {
        return waitForTitleContains(title, DEFAULT_TIMEOUT);
    }

    public static boolean waitForTitleContains(String title, int timeoutSeconds) {
        try {
            boolean result = getWait(timeoutSeconds).until(ExpectedConditions.titleContains(title));
            log.debug("Title contains '{}': {}", title, result);
            return result;
        } catch (Exception e) {
            log.error("Title does not contain '{}' within {} seconds", title, timeoutSeconds);
            return false;
        }
    }

    public static void waitForPageLoad() {
        waitForPageLoad(DEFAULT_TIMEOUT);
    }

    public static void waitForPageLoad(int timeoutSeconds) {
        try {
            getWait(timeoutSeconds).until(webDriver ->
                    ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
            log.debug("Page loaded successfully");
        } catch (Exception e) {
            log.warn("Page load wait timed out after {} seconds", timeoutSeconds);
        }
    }

    // Element disappearance waits
    public static boolean waitForElementToDisappear(By locator) {
        return waitForElementToDisappear(locator, DEFAULT_TIMEOUT);
    }

    public static boolean waitForElementToDisappear(By locator, int timeoutSeconds) {
        try {
            boolean result = getWait(timeoutSeconds).until(ExpectedConditions.invisibilityOfElementLocated(locator));
            log.debug("Element disappeared: {}", locator);
            return result;
        } catch (Exception e) {
            log.error("Element still visible after {} seconds: {}", timeoutSeconds, locator);
            return false;
        }
    }

    /**
     * Waits for a loader element to disappear. This method is useful for handling loading screens
     * that may or may not appear during page interactions.
     *
     * @param loaderLocator The locator for the loader element
     * @return true if loader disappeared or was never present, false if timeout occurred
     */
    public static boolean waitForLoaderToDisappear(By loaderLocator) {
        return waitForLoaderToDisappear(loaderLocator, DEFAULT_TIMEOUT);
    }

    /**
     * Waits for a loader element to disappear with custom timeout. This method first checks if
     * a loader appears, then waits for it to disappear.
     *
     * @param loaderLocator The locator for the loader element
     * @param timeoutSeconds Maximum time to wait for loader to disappear
     * @return true if loader disappeared or was never present, false if timeout occurred
     */
    public static boolean waitForLoaderToDisappear(By loaderLocator, int timeoutSeconds) {
        try {
            // First, briefly check if loader appears
            if (isElementPresent(loaderLocator, LOADER_DETECTION_TIMEOUT)) {
                log.debug("Loader detected, waiting for it to disappear: {}", loaderLocator);

                // Wait for the loader to disappear
                boolean disappeared = getWait(timeoutSeconds).until(ExpectedConditions.invisibilityOfElementLocated(loaderLocator));
                log.debug("Loader disappeared: {} (result: {})", loaderLocator, disappeared);
                return disappeared;
            } else {
                // Loader never appeared or disappeared very quickly
                log.debug("No loader detected or loader disappeared quickly: {}", loaderLocator);
                return true;
            }
        } catch (TimeoutException e) {
            log.warn("Loader still visible after {} seconds: {}", timeoutSeconds, loaderLocator);
            return false;
        } catch (Exception e) {
            log.error("Error while waiting for loader to disappear: {}", loaderLocator, e);
            return false;
        }
    }

    /**
     * Waits for multiple loaders to disappear. Useful when a page might have multiple loading indicators.
     *
     * @param loaderLocators Array of loader locators to wait for
     * @return true if all loaders disappeared or were never present
     */
    public static boolean waitForLoadersToDisappear(By... loaderLocators) {
        return waitForLoadersToDisappear(DEFAULT_TIMEOUT, loaderLocators);
    }

    /**
     * Waits for multiple loaders to disappear with custom timeout.
     *
     * @param timeoutSeconds Maximum time to wait for all loaders to disappear
     * @param loaderLocators Array of loader locators to wait for
     * @return true if all loaders disappeared or were never present
     */
    public static boolean waitForLoadersToDisappear(int timeoutSeconds, By... loaderLocators) {
        if (loaderLocators == null || loaderLocators.length == 0) {
            log.debug("No loader locators provided");
            return true;
        }

        boolean allDisappeared = true;
        for (By loaderLocator : loaderLocators) {
            if (!waitForLoaderToDisappear(loaderLocator, timeoutSeconds)) {
                allDisappeared = false;
                log.warn("Loader did not disappear within timeout: {}", loaderLocator);
            }
        }

        log.debug("All loaders disappeared: {}", allDisappeared);
        return allDisappeared;
    }

    // Element state checks
    public static boolean isElementPresent(By locator) {
        return isElementPresent(locator, SHORT_TIMEOUT);
    }

    public static boolean isElementPresent(By locator, int timeoutSeconds) {
        try {
            getWait(timeoutSeconds).until(ExpectedConditions.presenceOfElementLocated(locator));
            log.debug("Element is present: {}", locator);
            return true;
        } catch (Exception e) {
            log.debug("Element not present: {}", locator);
            return false;
        }
    }

    public static boolean isElementVisible(By locator) {
        return isElementVisible(locator, SHORT_TIMEOUT);
    }

    public static boolean isElementVisible(By locator, int timeoutSeconds) {
        try {
            getWait(timeoutSeconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
            log.debug("Element is visible: {}", locator);
            return true;
        } catch (Exception e) {
            log.debug("Element not visible: {}", locator);
            return false;
        }
    }

    public static boolean isDisplayed(By locator) {
        return isDisplayed(locator, SHORT_TIMEOUT);
    }

    public static boolean isDisplayed(By locator, int timeoutSeconds) {
        try {
            WebElement element = waitForElementPresent(locator, timeoutSeconds);
            boolean displayed = element.isDisplayed();
            log.debug("Element displayed status: {} for locator: {}", displayed, locator);
            return displayed;
        } catch (Exception e) {
            log.debug("Element not found or not displayed: {}", locator);
            return false;
        }
    }

    public static boolean isEnabled(By locator) {
        return isEnabled(locator, SHORT_TIMEOUT);
    }

    public static boolean isEnabled(By locator, int timeoutSeconds) {
        try {
            WebElement element = waitForElementPresent(locator, timeoutSeconds);
            boolean enabled = element.isEnabled();
            log.debug("Element enabled status: {} for locator: {}", enabled, locator);
            return enabled;
        } catch (Exception e) {
            log.debug("Element not found or not enabled: {}", locator);
            return false;
        }
    }

    public static boolean isSelected(By locator) {
        return isSelected(locator, SHORT_TIMEOUT);
    }

    public static boolean isSelected(By locator, int timeoutSeconds) {
        try {
            WebElement element = waitForElementPresent(locator, timeoutSeconds);
            boolean selected = element.isSelected();
            log.debug("Element selected status: {} for locator: {}", selected, locator);
            return selected;
        } catch (Exception e) {
            log.debug("Element not found or not selected: {}", locator);
            return false;
        }
    }

    /**
     * Waits for an element to be stale (no longer attached to DOM). Useful after page refreshes or navigation.
     *
     * @param element The WebElement to wait for staleness
     * @return true if element became stale, false otherwise
     */
    public static boolean waitForElementToBeStale(WebElement element) {
        return waitForElementToBeStale(element, DEFAULT_TIMEOUT);
    }

    /**
     * Waits for an element to be stale with custom timeout.
     *
     * @param element The WebElement to wait for staleness
     * @param timeoutSeconds Maximum time to wait
     * @return true if element became stale, false otherwise
     */
    public static boolean waitForElementToBeStale(WebElement element, int timeoutSeconds) {
        try {
            boolean isStale = getWait(timeoutSeconds).until(ExpectedConditions.stalenessOf(element));
            log.debug("Element became stale: {}", isStale);
            return isStale;
        } catch (Exception e) {
            log.debug("Element did not become stale within {} seconds", timeoutSeconds);
            return false;
        }
    }

    /**
     * Waits for text to be present in an element.
     *
     * @param locator The element locator
     * @param text The text to wait for
     * @return true if text is present, false otherwise
     */
    public static boolean waitForTextToBePresentInElement(By locator, String text) {
        return waitForTextToBePresentInElement(locator, text, DEFAULT_TIMEOUT);
    }

    /**
     * Waits for text to be present in an element with custom timeout.
     *
     * @param locator The element locator
     * @param text The text to wait for
     * @param timeoutSeconds Maximum time to wait
     * @return true if text is present, false otherwise
     */
    public static boolean waitForTextToBePresentInElement(By locator, String text, int timeoutSeconds) {
        try {
            boolean textPresent = getWait(timeoutSeconds).until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
            log.debug("Text '{}' present in element {}: {}", text, locator, textPresent);
            return textPresent;
        } catch (Exception e) {
            log.debug("Text '{}' not present in element {} within {} seconds", text, locator, timeoutSeconds);
            return false;
        }
    }

    /**
     * Waits for an attribute to contain a specific value.
     *
     * @param locator The element locator
     * @param attribute The attribute name
     * @param value The value to wait for
     * @return true if attribute contains value, false otherwise
     */
    public static boolean waitForAttributeContains(By locator, String attribute, String value) {
        return waitForAttributeContains(locator, attribute, value, DEFAULT_TIMEOUT);
    }

    /**
     * Waits for an attribute to contain a specific value with custom timeout.
     *
     * @param locator The element locator
     * @param attribute The attribute name
     * @param value The value to wait for
     * @param timeoutSeconds Maximum time to wait
     * @return true if attribute contains value, false otherwise
     */
    public static boolean waitForAttributeContains(By locator, String attribute, String value, int timeoutSeconds) {
        try {
            boolean attributeContains = getWait(timeoutSeconds).until(ExpectedConditions.attributeContains(locator, attribute, value));
            log.debug("Attribute '{}' contains '{}' in element {}: {}", attribute, value, locator, attributeContains);
            return attributeContains;
        } catch (Exception e) {
            log.debug("Attribute '{}' does not contain '{}' in element {} within {} seconds", attribute, value, locator, timeoutSeconds);
            return false;
        }
    }

    /**
     * Checks if element is actually clickable (not obscured by other elements).
     * This method could be moved to WaitUtils class as a utility method.
     */
    public static boolean isElementActuallyClickable(WebElement element) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();

            return (Boolean) js.executeScript(
                    "const el = arguments[0];" +
                            "const rect = el.getBoundingClientRect();" +
                            "const centerX = rect.left + rect.width / 2;" +
                            "const centerY = rect.top + rect.height / 2;" +
                            "const elementAtPoint = document.elementFromPoint(centerX, centerY);" +
                            "return el.offsetParent !== null && !el.disabled && " +
                            "       (elementAtPoint === el || el.contains(elementAtPoint));",
                    element
            );
        } catch (Exception e) {
            log.debug("Clickability check failed, assuming clickable: {}", e.getMessage());
            return true;
        }
    }

}