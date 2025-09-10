package com.balsamhill.automation.utils;

import com.balsamhill.automation.drivers.DriverManager;
import com.balsamhill.automation.logger.LoggerWrapper;
import org.openqa.selenium.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PageUtils {
    private static final LoggerWrapper log = new LoggerWrapper(PageUtils.class);

    private PageUtils() {
        // Private constructor to prevent instantiation
    }

    public static String getCurrentUrl() {
        return DriverManager.getDriver().getCurrentUrl();
    }

    public static String getPageTitle() {
        return DriverManager.getDriver().getTitle();
    }

    public static void refreshPage() {
        try {
            DriverManager.getDriver().navigate().refresh();
            WaitUtils.waitForPageLoad();
            log.info("Page refreshed");
        } catch (Exception e) {
            log.error("Failed to refresh page: {}", e.getMessage());
            throw new RuntimeException("Failed to refresh page", e);
        }
    }

    /**
     * Detects and dismisses the cookie consent banner if present.
     */
    public static void handleCookieBanner(WebDriver driver) {
        try {
            By cookieBannerBy = By.id("cookieBanner");
            WebElement cookieBanner = WaitUtils.waitForElementPresent(cookieBannerBy);
            if(cookieBanner.isDisplayed()) {
                log.step("Cookie banner detected, attempting to dismiss");

                // Try to find and click accept/close button
                List<By> cookieSelectors = Arrays.asList(
                        By.cssSelector("#cookieBanner button[data-testid*='accept']"),
                        By.cssSelector("#cookieBanner button[data-testid*='close']"),
                        By.cssSelector("#cookieBanner .btn-accept"),
                        By.cssSelector("#cookieBanner .close"),
                        By.cssSelector("#cookieBanner button[type='button']")
                );

                for(By selector : cookieSelectors) {
                    try {
                        WebElement cookieButton = driver.findElement(selector);

                        if (WaitUtils.isElementVisible(cookieBannerBy) && WaitUtils.isEnabled(cookieBannerBy)) {
                            WebElementUtils.click(cookieButton);
                            log.step("Cookie banner dismissed successfully");

                            return;
                        }
                    } catch (NoSuchElementException e) {
                        log.step("Cookie button not found with selector: {}", selector);
                    }
                }

                // If no button found, try JavaScript to hide the banner
                ((JavascriptExecutor) driver).executeScript(
                        "document.getElementById('cookieBanner').style.display = 'none';");
                log.step("Cookie banner hidden via JavaScript");
            }
        } catch (TimeoutException e) {
            log.step("No cookie banner found or already dismissed");
        } catch (Exception e) {
            log.step("Error handling cookie banner: {}", e.getMessage());
        }
    }

    public static void handleCookiePolicyBanner(WebDriver driver){
        // Try multiple possible selectors
        String[] selectors = {
                ".bButton_btn__6AYp1.p-0.btn-close.cookieBanner_btn-close__tt3Fr",
                "[class*='btn-close'][class*='cookieBanner']",
                ".cookie-banner .btn-close",
                "[aria-label='Close cookie banner']"
        };

        for (String selector : selectors) {
            try {
                By bannerBy = By.cssSelector(selector);
                WebElement cookieBanner = WaitUtils.waitForElementPresent(bannerBy, 2);
                cookieBanner.click();
                System.out.println("Cookie banner closed with selector: " + selector);
                return; // Success - exit method
            } catch (Exception e) {
                // Try next selector
                continue;
            }
        }
    }

}