package com.balsamhill.automation.pages;

import com.balsamhill.automation.drivers.DriverManager;
import com.balsamhill.automation.logger.LoggerWrapper;
import com.balsamhill.automation.utils.WebElementUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ProductDetailsModal {

    private static final LoggerWrapper log = new LoggerWrapper(ProductPage.class);

    private final WebDriver driver;

    private final By viewCart = By.cssSelector("button[data-testid='pdc-add-to-cart-modal-btn-viewcart']");

    /**
     * Constructor initializes the WebDriver instance.
     */
    public ProductDetailsModal(WebDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver cannot be null");
        }
        this.driver = driver;
    }

    /**
     * Clicks the "View Cart" button in the Product Details Modal.
     */
    public void viewCart() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            System.out.println("=== DEBUG: Looking for modal and View Cart button ===");
            System.out.println("Current URL: " + driver.getCurrentUrl());

            // Wait a moment for any modal to appear
            Thread.sleep(2000);

            // Check for any modal containers
            List<WebElement> allModals = driver.findElements(By.cssSelector("[class*='modal'], [id*='modal'], [data-testid*='modal']"));
            System.out.println("Found " + allModals.size() + " modal-like elements");

            // Check for the specific button
            List<WebElement> targetButtons = driver.findElements(By.cssSelector("button[data-testid='pdc-add-to-cart-modal-btn-viewcart']"));
            System.out.println("Found " + targetButtons.size() + " target view cart buttons");

            // Check for any view cart buttons by text
            List<WebElement> viewCartByText = driver.findElements(By.xpath("//button[contains(text(), 'View Cart') or contains(text(), 'VIEW CART')]"));
            System.out.println("Found " + viewCartByText.size() + " view cart buttons by text");

            // Check all buttons on page
            List<WebElement> allButtons = driver.findElements(By.tagName("button"));
            System.out.println("Total buttons on page: " + allButtons.size());

            // Print first 10 buttons for analysis
            for (int i = 0; i < Math.min(10, allButtons.size()); i++) {
                WebElement btn = allButtons.get(i);
                String text = btn.getText().trim();
                String testId = btn.getAttribute("data-testid");
                String classes = btn.getAttribute("class");
                boolean displayed = btn.isDisplayed();

                if (!text.isEmpty() || testId != null || (classes != null && classes.contains("cart"))) {
                    System.out.println("Button " + i + ": text='" + text + "', testid='" + testId + "', displayed=" + displayed + ", class='" + classes + "'");
                }
            }

            // Try to find and click any working view cart button
            WebElement viewCartButton = null;

            // First try: original selector
            if (!targetButtons.isEmpty() && targetButtons.get(0).isDisplayed()) {
                viewCartButton = targetButtons.get(0);
                System.out.println("Using original selector button");
            }
            // Second try: by text
            else if (!viewCartByText.isEmpty() && viewCartByText.get(0).isDisplayed()) {
                viewCartButton = viewCartByText.get(0);
                System.out.println("Using text-based button");
            }
            // Third try: look for cart-related buttons
            else {
                List<WebElement> cartButtons = driver.findElements(By.cssSelector("button[class*='cart'], button[data-testid*='cart']"));
                System.out.println("Found " + cartButtons.size() + " cart-related buttons");

                for (WebElement btn : cartButtons) {
                    if (btn.isDisplayed()) {
                        System.out.println("Cart button: text='" + btn.getText() + "', testid='" + btn.getAttribute("data-testid") + "'");
                        if (viewCartButton == null) {
                            viewCartButton = btn;
                            System.out.println("Using first cart-related button");
                            break;
                        }
                    }
                }
            }

            if (viewCartButton != null) {
                // Try to click the button
                try {
                    js.executeScript("arguments[0].scrollIntoView(true);", viewCartButton);
                    Thread.sleep(500);
                    viewCartButton.click();
                    System.out.println("SUCCESS: Clicked view cart button");
                } catch (Exception clickError) {
                    // Try JavaScript click
                    js.executeScript("arguments[0].click();", viewCartButton);
                    System.out.println("SUCCESS: JavaScript click worked");
                }
            } else {
                throw new RuntimeException("No view cart button found");
            }

        } catch (Exception e) {
            System.out.println("ERROR in viewCart: " + e.getMessage());
            throw new RuntimeException("Failed to click 'View Cart' button", e);
        }
    }

}
