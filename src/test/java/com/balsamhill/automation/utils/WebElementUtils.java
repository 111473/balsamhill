package com.balsamhill.automation.utils;

import com.balsamhill.automation.logger.LoggerWrapper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import com.balsamhill.automation.drivers.DriverManager;


public class WebElementUtils {
    private static final LoggerWrapper log = new LoggerWrapper(WebElementUtils.class);

    // Basic interactions using WaitUtils
    public static void click(By locator) {
        try {
            WebElement element = WaitUtils.waitForElementToBeClickable(locator);
            element.click();
            log.info("Clicked element: {}", locator);
        } catch (Exception e) {
            log.error("Failed to click element {}: {}", locator, e.getMessage());
            throw new RuntimeException("Failed to click element: " + locator, e);
        }
    }

    // Click method with WebElement parameter using existing WaitUtils methods
    public static void click(WebElement element) {
        try {
            WebElement clickableElement = WaitUtils.waitForElementToBeClickable(element);
            clickableElement.click();
            log.info("Clicked WebElement");
        } catch (Exception e) {
            log.error("Failed to click WebElement: {}", e.getMessage());
            throw new RuntimeException("Failed to click WebElement", e);
        }
    }

    // Click method with WebElement parameter and custom timeout
    public static void click(WebElement element, int timeoutSeconds) {
        try {
            WebElement clickableElement = WaitUtils.waitForElementToBeClickable(element, timeoutSeconds);
            clickableElement.click();
            log.info("Clicked WebElement with {}s timeout", timeoutSeconds);
        } catch (Exception e) {
            log.error("Failed to click WebElement within {} seconds: {}", timeoutSeconds, e.getMessage());
            throw new RuntimeException("Failed to click WebElement", e);
        }
    }

    public static void clickWithRetry(By locator, int maxAttempts) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < maxAttempts) {
            try {
                click(locator);
                return; // Success
            } catch (Exception e) {
                lastException = e;
                attempts++;
                log.warn("Click attempt {} failed for {}: {}", attempts, locator, e.getMessage());

                if (attempts < maxAttempts) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                }
            }
        }

        throw new RuntimeException("Failed to click after " + maxAttempts + " attempts: " + locator, lastException);
    }

    public static void type(By locator, String text) {
        try {
            WebElement element = WaitUtils.waitForElementToBeClickable(locator);
            element.clear();
            element.sendKeys(text);
            log.info("Typed '{}' into element: {}", text, locator);
        } catch (Exception e) {
            log.error("Failed to type into element {}: {}", locator, e.getMessage());
            throw new RuntimeException("Failed to type into element: " + locator, e);
        }
    }

    public static void typeWithoutClear(By locator, String text) {
        try {
            WebElement element = WaitUtils.waitForElementToBeClickable(locator);
            element.sendKeys(text);
            log.info("Typed '{}' into element (without clear): {}", text, locator);
        } catch (Exception e) {
            log.error("Failed to type into element {}: {}", locator, e.getMessage());
            throw new RuntimeException("Failed to type into element: " + locator, e);
        }
    }

    public static void pressKey(By locator, Keys key) {
        try {
            WebElement element = WaitUtils.waitForElementPresent(locator);
            element.sendKeys(key);
            log.info("Pressed {} on element: {}", key, locator);
        } catch (Exception e) {
            log.error("Failed to press {} on element {}: {}", key, locator, e.getMessage());
            throw new RuntimeException("Failed to press key on element: " + locator, e);
        }
    }

    public static void pressEnter(By locator) {
        pressKey(locator, Keys.ENTER);
    }

    public static void pressTab(By locator) {
        pressKey(locator, Keys.TAB);
    }

    // Advanced interactions
    public static void doubleClick(By locator) {
        try {
            WebElement element = WaitUtils.waitForElementToBeClickable(locator);
            Actions actions = new Actions(DriverManager.getDriver());
            actions.doubleClick(element).perform();
            log.info("Double-clicked element: {}", locator);
        } catch (Exception e) {
            log.error("Failed to double-click element {}: {}", locator, e.getMessage());
            throw new RuntimeException("Failed to double-click element: " + locator, e);
        }
    }

    public static void rightClick(By locator) {
        try {
            WebElement element = WaitUtils.waitForElementToBeClickable(locator);
            Actions actions = new Actions(DriverManager.getDriver());
            actions.contextClick(element).perform();
            log.info("Right-clicked element: {}", locator);
        } catch (Exception e) {
            log.error("Failed to right-click element {}: {}", locator, e.getMessage());
            throw new RuntimeException("Failed to right-click element: " + locator, e);
        }
    }

    public static void hoverOver(By locator) {
        try {
            WebElement element = WaitUtils.waitForElementToBeVisible(locator);
            Actions actions = new Actions(DriverManager.getDriver());
            actions.moveToElement(element).perform();
            log.info("Hovered over element: {}", locator);
        } catch (Exception e) {
            log.error("Failed to hover over element {}: {}", locator, e.getMessage());
            throw new RuntimeException("Failed to hover over element: " + locator, e);
        }
    }

    // Scrolling utilities
    public static void scrollToElement(By locator) {
        try {
            WebElement element = WaitUtils.waitForElementPresent(locator);
            scrollToElement(element);
        } catch (Exception e) {
            log.error("Failed to scroll to element {}: {}", locator, e.getMessage());
            throw new RuntimeException("Failed to scroll to element: " + locator, e);
        }
    }

    public static void scrollToElement(WebElement element) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
            log.info("Scrolled to element: {}", element);
        } catch (Exception e) {
            log.error("Failed to scroll to element: {}", e.getMessage());
            throw new RuntimeException("Failed to scroll to element", e);
        }
    }

    public static void scrollToTop() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
            js.executeScript("window.scrollTo(0, 0);");
            log.info("Scrolled to top of page");
        } catch (Exception e) {
            log.error("Failed to scroll to top: {}", e.getMessage());
        }
    }

    public static void scrollToBottom() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            log.info("Scrolled to bottom of page");
        } catch (Exception e) {
            log.error("Failed to scroll to bottom: {}", e.getMessage());
        }
    }

    // JavaScript interactions
    public static void clickUsingJavaScript(By locator) {
        try {
            WebElement element = WaitUtils.waitForElementPresent(locator);
            JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
            js.executeScript("arguments[0].click();", element);
            log.info("Clicked element using JavaScript: {}", locator);
        } catch (Exception e) {
            log.error("Failed to click element using JavaScript {}: {}", locator, e.getMessage());
            throw new RuntimeException("Failed to click element using JavaScript: " + locator, e);
        }
    }

    // Element property getters
    public static String getText(By locator) {
        try {
            WebElement element = WaitUtils.waitForElementToBeVisible(locator);
            String text = element.getText().trim();
            log.debug("Got text '{}' from element: {}", text, locator);
            return text;
        } catch (Exception e) {
            log.error("Failed to get text from element {}: {}", locator, e.getMessage());
            throw new RuntimeException("Failed to get text from element: " + locator, e);
        }
    }

    public static String getAttribute(By locator, String attributeName) {
        try {
            WebElement element = WaitUtils.waitForElementPresent(locator);
            String value = element.getAttribute(attributeName);
            log.debug("Got attribute '{}' = '{}' from element: {}", attributeName, value, locator);
            return value;
        } catch (Exception e) {
            log.error("Failed to get attribute '{}' from element {}: {}", attributeName, locator, e.getMessage());
            throw new RuntimeException("Failed to get attribute from element: " + locator, e);
        }
    }

}