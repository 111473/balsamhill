package com.balsamhill.automation.tests;

import com.balsamhill.automation.base.BaseTest;
import com.balsamhill.automation.drivers.DriverManager;
import com.balsamhill.automation.logger.LoggerWrapper;
import com.balsamhill.automation.models.SearchTestData;
import com.balsamhill.automation.pages.*;
import com.balsamhill.automation.reports.AllureReportManager;
import com.balsamhill.automation.utils.AssertionUtils;
import com.balsamhill.automation.utils.TestDataUtils;
import io.qameta.allure.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Map;

@Epic("E-Commerce Testing")
@Feature("Product Search and Pricing")
public class BalsamHillSearchTests extends BaseTest {

    private static final LoggerWrapper log = new LoggerWrapper(BalsamHillSearchTests.class);

    private LoginPage loginPage;
    private MyAccountPage myAccountPage;
    private SearchResultsPage searchResultsPage;
    private ProductPage productPage;
    private ProductDetailsModal productDetailsModal;
    private ShoppingCartPage shoppingCartPage;

    // Store current browser for reporting
    private String currentBrowser;

    @BeforeMethod
    @Parameters({"browser", "environment"})
    @Step("Set up test environment and initialize page objects")
    public void setUp(@Optional("chrome") String browser, @Optional("staging") String environment) {
        // Store browser info for reporting
        this.currentBrowser = browser;

        // Add browser info to Allure report
        Allure.parameter("Browser", browser);
        Allure.parameter("Environment", environment);

        // Call parent setUp which initializes the driver with browser parameter
        super.setUp(browser, environment);

        // Get the driver from DriverManager
        WebDriver driver = DriverManager.getDriver();

        // Debug log to verify driver is not null
        log.info("Driver from DriverManager for browser {}: {}", browser, driver);
        Allure.addAttachment("Driver Info",
                String.format("Browser: %s, Driver initialized: %s", browser, (driver != null ? "Success" : "Failed")));

        // Initialize page objects with the driver
        loginPage = new LoginPage(driver);
        myAccountPage = new MyAccountPage(driver);
        searchResultsPage = new SearchResultsPage(driver);
        productPage = new ProductPage(driver);
        productDetailsModal = new ProductDetailsModal(driver);
        shoppingCartPage = new ShoppingCartPage(driver);

        log.info("Page objects initialized successfully for browser: {}", browser);
        Allure.step(String.format("Page objects initialized successfully for browser: %s", browser));
    }

    @Test(dataProvider = "searchDataProvider", dataProviderClass = TestDataUtils.class,
            groups = {"smoke", "search", "pricing"})
    @Story("Price Validation on Search Results")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that product price on search results page matches the expected price from test data without any customization")
    @Issue("BALSAM-001")
    public void testVerifyPriceOnSearchResultsWithoutProductCustomization(SearchTestData testData)
            throws InterruptedException {

        // Add test parameters to Allure report
        Allure.parameter("Search Term", testData.getSearchTerm());
        Allure.parameter("Expected Price", testData.getCurrentPrice());
        Allure.parameter("Product Index", testData.getProductIndex());
        Allure.parameter("Browser", currentBrowser);

        log.step("Starting price validation test on search results for browser: {}", currentBrowser);

        performLogin();
        performSearch(testData.getSearchTerm());

        String expectedPrice = searchResultsPage.fetchBasePriceFromJson(testData.getCurrentPrice());
        String actualPrice = searchResultsPage.getDisplayedProductPrice();

        Allure.step("Expected Price: " + expectedPrice);
        Allure.step("Actual Price: " + actualPrice);

        AssertionUtils.assertEquals(
                expectedPrice,
                actualPrice,
                String.format("Verification confirmed that the product base price from the Search Results Page matched the price in the Test Data on browser: %s", currentBrowser)
        );

        attachScreenshot(String.format("Validate Price on Search Result Page without Product Customization - %s", currentBrowser));
        Allure.step(String.format("Price validation completed successfully on Search Results page for browser: %s", currentBrowser));
    }

    @Test(dataProvider = "searchDataProvider", dataProviderClass = TestDataUtils.class,
            groups = {"regression", "search", "pricing"})
    @Story("Price Validation on Product Details")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that product price on product details page matches the expected price from test data without any customization")
    @Issue("BALSAM-002")
    public void testVerifyProductPriceWithoutProductCustomization(SearchTestData testData)
            throws InterruptedException {

        // Add test parameters to Allure report
        Allure.parameter("Search Term", testData.getSearchTerm());
        Allure.parameter("Expected Price", testData.getCurrentPrice());
        Allure.parameter("Product Index", testData.getProductIndex());
        Allure.parameter("Browser", currentBrowser);

        log.step("Starting price validation test on product details for browser: {}", currentBrowser);

        performLogin();
        performSearch(testData.getSearchTerm());
        selectProduct(testData.getProductIndex());

        String expectedPrice = searchResultsPage.fetchBasePriceFromJson(testData.getCurrentPrice());
        String actualPrice = productPage.getDisplayedProductPrice();

        Allure.step("Expected Price: " + expectedPrice);
        Allure.step("Actual Price: " + actualPrice);

        AssertionUtils.assertEquals(
                expectedPrice,
                actualPrice,
                String.format("Verified: Product base price from the Search Results Page matches the price in the Test Data on browser: %s", currentBrowser)
        );

        attachScreenshot(String.format("Validate Price on Product Details Page without Product Customization - %s", currentBrowser));
        Allure.step(String.format("Price validation completed successfully on Product Details page for browser: %s", currentBrowser));
    }

    @Test(dataProvider = "searchDataProvider", dataProviderClass = TestDataUtils.class,
            groups = {"regression", "cart", "pricing"})
    @Story("Price Validation on Shopping Cart")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that product price in shopping cart matches the expected price from test data without any customization")
    @Issue("BALSAM-003")
    public void testVerifyCartPriceWithoutProductCustomization(SearchTestData testData)
            throws InterruptedException {

        // Add test parameters to Allure report
        Allure.parameter("Search Term", testData.getSearchTerm());
        Allure.parameter("Expected Price", testData.getCurrentPrice());
        Allure.parameter("Product Index", testData.getProductIndex());
        Allure.parameter("Browser", currentBrowser);

        log.step("Starting cart price validation test for browser: {}", currentBrowser);

        performLogin();
        performSearch(testData.getSearchTerm());

        Thread.sleep(10000); // Consider replacing with explicit wait
        Allure.step("Waiting for page to load completely");

        selectProduct(testData.getProductIndex());
        addProductToCart();
        navigateToCart();

        String expectedPrice = searchResultsPage.fetchBasePriceFromJson(testData.getCurrentPrice());
        String actualPrice = shoppingCartPage.getDisplayedProductPrice();

        Allure.step("Expected Price: " + expectedPrice);
        Allure.step("Actual Price: " + actualPrice);

        AssertionUtils.assertEquals(
                expectedPrice,
                actualPrice,
                String.format("Verified: Product base price from the Product Page matches the price in the Test Data on browser: %s", currentBrowser)
        );

        attachScreenshot(String.format("Validate Price on Shopping Cart Page without Product Customization - %s", currentBrowser));
        Allure.step(String.format("Price validation completed successfully on Shopping Cart page for browser: %s", currentBrowser));
    }

    @Test(dataProvider = "searchDataProvider", dataProviderClass = TestDataUtils.class,
            groups = {"regression", "cart", "customization"})
    @Story("Price Validation with Product Customization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that customized product price in shopping cart matches the adjusted price after customization options are selected")
    @Issue("BALSAM-004")
    public void testVerifyCartPriceWithProductCustomization(SearchTestData testData)
            throws InterruptedException {

        // Add test parameters to Allure report
        Allure.parameter("Search Term", testData.getSearchTerm());
        Allure.parameter("Base Price", testData.getCurrentPrice());
        Allure.parameter("Product Index", testData.getProductIndex());
        Allure.parameter("Customization Options", testData.getCustomizationOptions().toString());
        Allure.parameter("Browser", currentBrowser);

        log.step("Starting customization price validation test for browser: {}", currentBrowser);

        performLogin();
        performSearch(testData.getSearchTerm());
        selectProduct(testData.getProductIndex());

        applyCustomizations(testData.getCustomizationOptions());
        String adjustedPrice = productPage.getDisplayedProductPrice();
        Allure.step("Price after customization: " + adjustedPrice);

        addProductToCart();
        navigateToCart();

        String cartPrice = shoppingCartPage.getDisplayedProductPrice();
        Allure.step("Price in shopping cart: " + cartPrice);

        AssertionUtils.assertEquals(
                adjustedPrice,
                cartPrice,
                String.format("Verified: Product base price from the Search Results Page matches the price in the Test Data on browser: %s", currentBrowser)
        );

        attachScreenshot(String.format("Validate Price on Shopping Cart Page with Product Customization - %s", currentBrowser));
        Allure.step(String.format("Price validation with customization completed successfully for browser: %s", currentBrowser));

        removeItem();
    }

    @Test(dataProvider = "searchDataProvider", dataProviderClass = TestDataUtils.class,
            groups = {"smoke", "cart"})
    @Story("Display Cart Item Count After Adding Product")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that the cart icon shows one item after adding a product.")
    @Issue("BALSAM-005")
    public void testValidateCartIconShowsOneItem(SearchTestData testData)
            throws InterruptedException {

        // Add test parameters to Allure report
        Allure.parameter("Search Term", testData.getSearchTerm());
        Allure.parameter("Base Price", testData.getCurrentPrice());
        Allure.parameter("Product Index", testData.getProductIndex());
        Allure.parameter("Customization Options", testData.getCustomizationOptions().toString());
        Allure.parameter("Browser", currentBrowser);

        log.step("Starting cart icon validation test for browser: {}", currentBrowser);

        performLogin();
        performSearch(testData.getSearchTerm());
        selectProduct(testData.getProductIndex());

        applyCustomizations(testData.getCustomizationOptions());
        String adjustedPrice = productPage.getDisplayedProductPrice();
        Allure.step("Price after customization: " + adjustedPrice);

        addProductToCart();
        navigateToCart();

        String cartPrice = shoppingCartPage.getDisplayedProductPrice();
        Allure.step("Price in shopping cart: " + cartPrice);

        AssertionUtils.assertTrue(
                shoppingCartPage.isCartIconItemCountDisplayed(),
                String.format("Cart icon should display '1' after adding a product on browser: %s", currentBrowser));

        attachScreenshot(String.format("Cart icon displays 1 after adding an item - %s", currentBrowser));

        removeItem();
    }

    @Test(dataProvider = "searchDataProvider", dataProviderClass = TestDataUtils.class,
            groups = {"regression", "cart"})
    @Story("Display Item Removal Confirmation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that the cart shows removal confirmation after removing an item.")
    @Issue("BALSAM-006")
    public void testValidateRemovalConfirmationMessage(SearchTestData testData)
            throws InterruptedException {

        // Add test parameters to Allure report
        Allure.parameter("Search Term", testData.getSearchTerm());
        Allure.parameter("Base Price", testData.getCurrentPrice());
        Allure.parameter("Product Index", testData.getProductIndex());
        Allure.parameter("Customization Options", testData.getCustomizationOptions().toString());
        Allure.parameter("Browser", currentBrowser);

        log.step("Starting removal confirmation test for browser: {}", currentBrowser);

        performLogin();
        performSearch(testData.getSearchTerm());
        selectProduct(testData.getProductIndex());

        applyCustomizations(testData.getCustomizationOptions());
        String adjustedPrice = productPage.getDisplayedProductPrice();
        Allure.step("Price after customization: " + adjustedPrice);

        addProductToCart();
        navigateToCart();

        String cartPrice = shoppingCartPage.getDisplayedProductPrice();
        Allure.step("Price in shopping cart: " + cartPrice);

        removeItem();
        Allure.step("Product Item has been removed");

        AssertionUtils.assertTrue(
                shoppingCartPage.isKeyWordHasBeenRemovedDisplayed(),
                String.format("Item removal confirmation should be displayed on browser: %s", currentBrowser));

        attachScreenshot(String.format("Removal confirmation dialog displays 'Item has been removed' - %s", currentBrowser));
    }

    // Helper methods with Allure steps
    @Step("Perform user login")
    private void performLogin() {
        try {
            loginPage.login();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Allure.step(String.format("User logged in successfully on browser: %s", currentBrowser));
    }

    @Step("Remove product item")
    private void removeItem(){
        shoppingCartPage.deleteItem();
        Allure.step(String.format("Remove Product Item on browser: %s", currentBrowser));
    }

    @Step("Search for product: {searchTerm}")
    private void performSearch(String searchTerm) {
        myAccountPage.search(searchTerm);
        Allure.step(String.format("Search performed for: %s on browser: %s", searchTerm, currentBrowser));
    }

    @Step("Select product at index: {productIndex}")
    private void selectProduct(int productIndex) {
        searchResultsPage.selectProduct(productIndex);
        Allure.step(String.format("Product selected at index: %d on browser: %s", productIndex, currentBrowser));
    }

    @Step("Apply product customizations")
    private void applyCustomizations(Object customizationOptions) {
        productPage.selectOptions((Map<String, String>) customizationOptions);
        Allure.step(String.format("Customization options applied: %s on browser: %s", customizationOptions.toString(), currentBrowser));
    }

    @Step("Add product to shopping cart")
    private void addProductToCart() {
        productPage.addToCart();
        Allure.step(String.format("Product added to cart successfully on browser: %s", currentBrowser));
    }

    @Step("Navigate to shopping cart")
    private void navigateToCart() {
        productDetailsModal.viewCart();
        Allure.step(String.format("Navigated to shopping cart on browser: %s", currentBrowser));
    }

    @Step("Remove item from shopping cart")
    private void removeItemFromCart() {
        productDetailsModal.viewCart();
        Allure.step(String.format("Removed Item from shopping cart on browser: %s", currentBrowser));
    }

    @Attachment(value = "{name}", type = "image/png")
    private void attachScreenshot(String name) {
        WebDriver driver = DriverManager.getDriver();

        if (driver == null) {
            log.error("WebDriver is null when trying to capture screenshot: {} on browser: {}", name, currentBrowser);
            Allure.step("Failed to capture screenshot - WebDriver is null");
            return;
        }

        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            AllureReportManager.attachScreenshot(name, screenshot);
            log.info("Screenshot captured: {} on browser: {}", name, currentBrowser);
            Allure.step(String.format("Screenshot captured: %s on browser: %s", name, currentBrowser));
        } catch (Exception e) {
            log.error("Failed to capture screenshot: {} on browser: {} due to {}", name, currentBrowser, e.getMessage());
            Allure.step(String.format("Failed to capture screenshot on browser %s: %s", currentBrowser, e.getMessage()));
        }
    }

    @Attachment(value = "Test Data", type = "application/json")
    public String attachTestData(SearchTestData testData) {
        // You can serialize your test data to JSON and attach it
        return String.format("Browser: %s, Test Data: %s", currentBrowser, testData.toString());
    }

    @Attachment(value = "Browser Console Logs", type = "text/plain")
    public String attachBrowserLogs() {
        // Implementation to capture and attach browser console logs
        WebDriver driver = DriverManager.getDriver();
        return String.format("Browser: %s, Console logs captured", currentBrowser);
    }
}