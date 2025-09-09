package com.balsamhill.automation.utils;

import com.balsamhill.automation.drivers.DriverManager;
import com.balsamhill.automation.logger.LoggerWrapper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


public class WaitUtils {
    private static final LoggerWrapper log = new LoggerWrapper(WaitUtils.class);
    private static final int DEFAULT_TIMEOUT = 10;
    private static final int SHORT_TIMEOUT = 5;

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

    // Element state checks
    public static boolean isElementPresent(By locator) {
        return isElementPresent(locator, SHORT_TIMEOUT);
    }

    public static boolean isElementPresent(By locator, int timeoutSeconds) {
        try {
            getWait(timeoutSeconds).until(ExpectedConditions.presenceOfElementLocated(locator));
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
}

