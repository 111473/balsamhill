package com.balsamhill.automation.pages;

import com.balsamhill.automation.logger.LoggerWrapper;
import com.balsamhill.automation.utils.PageUtils;
import com.balsamhill.automation.utils.WaitUtils;
import com.balsamhill.automation.utils.WebElementUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductPage {

    private static final LoggerWrapper log = new LoggerWrapper(ProductPage.class);

    private final WebDriver driver;

    private final By sizeList = By.cssSelector("div[class*='small-product-filter-box'][class*='productDetailFilter_product-filter-item']");
    private final By lightList = By.cssSelector("div[class*='large-product-filter-box'][class*='productDetailFilter_product-filter-item']");
    private final By addToCartButton = By.cssSelector("div.row.align-items-center.h-100 button");
    private final By price = By.cssSelector("div.productPrice_old-new-price__a0Rwo span.product-price");

    /**
     * Constructor initializes the WebDriver instance.
     */
    public ProductPage(WebDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver cannot be null");
        }
        this.driver = driver;
    }

    /**
     * Selects product options based on provided customization options.
     *
     * @param customizationOptions Map with keys "size" and "lightType" for desired selections
     * @throws InterruptedException
     */
    public void selectOptions(Map<String, String> customizationOptions) {
        // Handle cookie banner first
        PageUtils.handleCookieBanner(driver);

        String desiredSize = customizationOptions.get("size");
        String desiredLight = customizationOptions.get("lightType");

        log.step("Desired size: {}", desiredSize);
        log.step("Desired light: {}", desiredLight);

        // Handle size selection
        if(desiredSize != null && !desiredSize.isEmpty()) {
            selectSize(desiredSize);
            log.step("Size option selected: {}", desiredSize);
        }

        // Handle light selection
        if(desiredLight != null && !desiredLight.isEmpty()) {
            selectLight(desiredLight);
            log.step("Light option selected: {}", desiredLight);
        }
    }

    public String getDisplayedProductPrice() {
        try {
            WebElement priceElement = WaitUtils.waitForElementPresent(price);
            String priceText = priceElement.getText().trim();
            log.step("Product price found: {}", priceText);
            return priceText;
        } catch (Exception e) {
            log.error("Failed to retrieve product price from the Product Page.: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve product price from the Product Page.", e);
        }
    }

    public void addToCart() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Try the original selector first
            List<WebElement> targetButtons = driver.findElements(addToCartButton);

            if (!targetButtons.isEmpty()) {
                WebElement button = targetButtons.get(0);

                if (button.isDisplayed() && button.isEnabled()) {
                    // Try multiple click approaches
                    if (tryClickMethods(button, wait, js)) {
                        return;
                    }
                }
            }

            // Fallback: Search for Add to Cart buttons by text
            List<WebElement> addToCartButtons = driver.findElements(
                    By.xpath("//button[contains(text(), 'Add to Cart') or contains(text(), 'ADD TO CART') or contains(@aria-label, 'Add to Cart')]")
            );

            for (WebElement btn : addToCartButtons) {
                if (btn.isDisplayed() && btn.isEnabled()) {
                    if (tryClickMethods(btn, wait, js)) {
                        return;
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to click 'Add to Cart' button", e);
        }
    }

    private boolean tryClickMethods(WebElement button, WebDriverWait wait, JavascriptExecutor js) {
        // Method 1: Standard click with scroll
        try {
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", button);
            Thread.sleep(500);
            wait.until(ExpectedConditions.elementToBeClickable(button));
            button.click();
            return true;
        } catch (Exception e1) {
            // Continue to next method
        }

        // Method 2: JavaScript click
        try {
            js.executeScript("arguments[0].click();", button);
            return true;
        } catch (Exception e2) {
            // Continue to next method
        }

        // Method 3: Actions click
        try {
            Actions actions = new Actions(driver);
            actions.moveToElement(button).click().perform();
            return true;
        } catch (Exception e3) {
            // All methods failed
        }

        return false;
    }

    /**
     * Selects the desired size from available options.
     *
     * @param desiredSize The size to select (e.g., "7.5'", "9'")
     * @throws InterruptedException
     */
    private void selectSize(String desiredSize) {
        for(int i = 0; i < 3; i++) { // Retry up to 3 times
            try {
                List<WebElement> sizes = getSizes(sizeList);
                log.step("Available sizes: {}", sizes.size());

                for(int j = 0; j < sizes.size(); j++){
                    // Re-find elements to avoid stale reference
                    sizes = getSizes(sizeList);

                    if(j >= sizes.size()) break;

                    WebElement size = sizes.get(j);
                    String sizeText = size.getText().trim();
                    log.step("Checking size option: '{}'", sizeText);

                    if(sizeText.toLowerCase().contains(desiredSize.toLowerCase())){
                        log.step("Found matching size: {}", sizeText);

                        // Ensure cookie banner is still dismissed
                        PageUtils.handleCookieBanner(driver);

                        WebElementUtils.scrollToElement(size);
                        WebElementUtils.click(size);
                        log.step("Successfully clicked size: {}", sizeText);
                        return;
                    }
                }
                break;

            } catch (ElementClickInterceptedException e) {
                log.step("Click intercepted on attempt {}, handling overlays and retrying", i + 1);
//                handleCookieBanner();
            } catch (Exception e) {
                log.step("Error selecting size on attempt {}: {}", i + 1, e.getMessage());
            }
        }
    }

    /**
     * Selects the desired light type from available options.
     *
     * @param desiredLight The light type to select (e.g., "Clear", "Multicolor")
     */
    private void selectLight(String desiredLight) {
        for(int i = 0; i < 3; i++) { // Retry up to 3 times
            try {
                List<WebElement> lights = getLights(lightList);
                log.step("Available lights: {}", lights.size());

                for(int j = 0; j < lights.size(); j++){
                    // Re-find elements to avoid stale reference
                    lights = getLights(lightList);

                    if(j >= lights.size()) break;

                    WebElement light = lights.get(j);
                    String lightText = light.getText().trim().replaceAll("[^0-9a-zA-Z\\s]", "");
                    log.step("Checking light option: '{}'", lightText);

                    if(lightText.equalsIgnoreCase(desiredLight)){
                        log.step("Found matching light: {}", lightText);

                        // Ensure cookie banner is still dismissed
                        PageUtils.handleCookieBanner(driver);

                        WebElementUtils.scrollToElement(light);
                        WebElementUtils.click(light);
                        log.step("Successfully clicked light: {}", lightText);
                        return;
                    }
                }
                break; // Exit retry loop if no matching light found

            } catch (ElementClickInterceptedException e) {
                log.step("Click intercepted on attempt {}, handling overlays and retrying", i + 1);
//                handleCookieBanner();
            } catch (Exception e) {
                log.step("Error selecting light on attempt {}: {}", i + 1, e.getMessage());
            }
        }
    }

    /**
     * Collects all size options on the page.
     *
     * @param sizes By locator for the size options to collect
     * @return List of all collected WebElements representing sizes
     */
    private List<WebElement> getSizes(By sizes) {

        List<WebElement> allItems = new ArrayList<>();

        try {
            while (true) {
                // Use the new WaitUtils method
                List<WebElement> xmastreeSizes = WaitUtils.findElementsWithWait(sizeList);

                for (WebElement item : xmastreeSizes) {
                    allItems.add(item);
                }
                break;
            }
        } catch (Exception e) {
            log.error("Error during item collection: {}", e.getMessage());
        }

        return allItems;
    }

    /**
     * Collects all light options on the page.
     *
     * @param sizes By locator for the light options to collect
     * @return List of all collected WebElements representing lights
     */
    private List<WebElement> getLights(By sizes) {

        List<WebElement> allItems = new ArrayList<>();

        try {
            while (true) {
                // Use the new WaitUtils method
                List<WebElement> xmasTreeLights = WaitUtils.findElementsWithWait(lightList);

                for (WebElement item : xmasTreeLights) {
                    allItems.add(item);
                }
                break;
            }
        } catch (Exception e) {
            log.error("Error during item collection: {}", e.getMessage());
        }

        return allItems;
    }

}



