package com.balsamhill.automation.tests;

import com.balsamhill.automation.base.BaseTest;
import com.balsamhill.automation.drivers.DriverManager;
import com.balsamhill.automation.logger.LoggerWrapper;
import com.balsamhill.automation.models.SearchTestData;
import com.balsamhill.automation.pages.*;
import com.balsamhill.automation.reports.AllureReportManager;
import com.balsamhill.automation.utils.AssertionUtils;
import com.balsamhill.automation.utils.TestDataUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.qameta.allure.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Epic("E-Commerce Testing")
@Feature("Product Search and Pricing")
public class BalsamHillSearchTests extends BaseTest {

    private static final LoggerWrapper log = new LoggerWrapper(BalsamHillSearchTests.class);
    private static final String JSON_OUTPUT_DIR = "test-results/price-capture/";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // Page Objects
    private LoginPage loginPage;
    private MyAccountPage myAccountPage;
    private SearchResultsPage searchResultsPage;
    private ProductPage productPage;
    private ProductDetailsModal productDetailsModal;
    private ShoppingCartPage shoppingCartPage;

    // JSON Utility
    private ObjectMapper objectMapper;

    // Store current browser for reporting
    private String currentBrowser;
    private String testStartTime;

    @BeforeMethod
    @Parameters({"browser", "environment"})
    @Step("Set up test environment and initialize page objects")
    public void setUp(@Optional("chrome") String browser, @Optional("staging") String environment) {
        this.currentBrowser = browser;
        this.testStartTime = LocalDateTime.now().format(DATE_FORMAT);

        // Initialize JSON mapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Add browser info to Allure report
        Allure.parameter("Browser", browser);
        Allure.parameter("Environment", environment);
        Allure.parameter("Test Start Time", testStartTime);

        // Call parent setUp which initializes the driver
        super.setUp(browser, environment);

        // Get the driver and initialize page objects
        WebDriver driver = DriverManager.getDriver();
        initializePageObjects(driver);

        log.info("Test setup completed successfully for browser: {} at {}", browser, testStartTime);
        Allure.step(String.format("Test environment initialized for browser: %s", browser));
    }

    @Test(dataProvider = "searchDataProvider", dataProviderClass = TestDataUtils.class,
            groups = {"regression", "cart", "pricing"})
    @Story("Complete Price Journey Validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Validate price consistency throughout the entire purchase journey")
    @Issue("BALSAM-001")
    public void testCompletePriceJourneyWithCapture(SearchTestData testData)
            throws InterruptedException {

        addTestParametersToReport(testData);
        log.step("Starting complete price journey test for browser: {}", currentBrowser);

        performLogin();
        performSearch(testData.getSearchTerm());

        // Capture price at each step
        String searchResultsPrice = searchResultsPage.selectProductAndGetPrice(testData.getProductIndex());

        String productDetailsPrice = productPage.getDisplayedProductPrice();
        addProductToCart();
        navigateToCart();

        String cartPrice = shoppingCartPage.getDisplayedProductPrice();

        // Create comprehensive test data
        SearchTestData journeyData = new SearchTestData.Builder()
                .searchTerm(testData.getSearchTerm())
                .productIndex(testData.getProductIndex())
                .currentPrice(searchResultsPrice)
                .expectedPrice(testData.getCurrentPrice())
                .newPrice(cartPrice)
                .expectedItemName(testData.getExpectedItemName())
                .build();

        String jsonFilePath = saveTestDataToJson(journeyData, "complete-journey");
        attachJsonData(journeyData, "Complete Journey Price Data");

        // Validate price consistency
        Allure.step("Base Price: " + searchResultsPrice);
        Allure.step("Customized Price: " + productDetailsPrice);
        Allure.step("Cart Price: " + cartPrice);

        AssertionUtils.assertEquals(searchResultsPrice, productDetailsPrice,
                "Price should be consistent between search results and product details");
        AssertionUtils.assertEquals(productDetailsPrice, cartPrice,
                "Price should be consistent between product details and cart");

        attachScreenshot("Complete Price Journey Validation");

        // Cleanup
        removeItem();

        log.info("Complete price journey validation completed. Data saved to: {}", jsonFilePath);
    }

    @Test(dataProvider = "searchDataProvider", dataProviderClass = TestDataUtils.class,
            groups = {"regression", "cart", "customization"})
    @Story("Price Validation with Product Customization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify customized product price changes and capture all price variations")
    @Issue("BALSAM-002")
    public void testCustomizationPriceValidationWithCapture(SearchTestData testData)
            throws InterruptedException {

        addTestParametersToReport(testData);
        log.step("Starting customization price validation for browser: {}", currentBrowser);

        performLogin();
        performSearch(testData.getSearchTerm());

        // Capture base price before customization
        String searchResultsPrice = searchResultsPage.selectProductAndGetPrice(testData.getProductIndex());

        // Apply customizations
        applyCustomizations(testData.getCustomizationOptions());

        // Capture price after customization
        String customizedPrice = productPage.getDisplayedProductPrice();

        addProductToCart();
        navigateToCart();

        String cartPrice = shoppingCartPage.getDisplayedProductPrice();

        // Create detailed customization test data
        SearchTestData customizationData = new SearchTestData.Builder()
                .searchTerm(testData.getSearchTerm())
                .productIndex(testData.getProductIndex())
                .currentPrice(searchResultsPrice)
                .newPrice(customizedPrice)
                .expectedPrice(cartPrice)
                .customizationOptions(testData.getCustomizationOptions())
                .build();

        String jsonFilePath = saveTestDataToJson(customizationData, "customization");
        attachJsonData(customizationData, "Customization Price Data");

        // Validation
        Allure.step("Base Price: " + searchResultsPrice);
        Allure.step("Customized Price: " + customizedPrice);
        Allure.step("Cart Price: " + cartPrice);

        AssertionUtils.assertEquals(customizedPrice, cartPrice,
                "Customized price should match cart price");

        attachScreenshot("Customization Price Validation");

        removeItem();

        log.info("Customization validation completed. Data saved to: {}", jsonFilePath);
    }

    @Test(dataProvider = "searchDataProvider", dataProviderClass = TestDataUtils.class,
            groups = {"smoke", "cart"})
    @Story("Display Cart Item Count After Adding Product")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that the cart icon shows one item after adding a product.")
    @Issue("BALSAM-003")
    public void testValidateCartIconShowsOneItem(SearchTestData testData)
            throws InterruptedException {

        addTestParametersToReport(testData);
        log.step("Starting customization price validation for browser: {}", currentBrowser);

        performLogin();
        performSearch(testData.getSearchTerm());

        // Capture base price before customization
        String searchResultsPrice = searchResultsPage.selectProductAndGetPrice(testData.getProductIndex());

        // Apply customizations
        applyCustomizations(testData.getCustomizationOptions());

        // Capture price after customization
        String customizedPrice = productPage.getDisplayedProductPrice();

        addProductToCart();
        navigateToCart();

        String cartPrice = shoppingCartPage.getDisplayedProductPrice();

        // Create detailed customization test data
        SearchTestData customizationData = new SearchTestData.Builder()
                .searchTerm(testData.getSearchTerm())
                .productIndex(testData.getProductIndex())
                .currentPrice(searchResultsPrice)
                .newPrice(customizedPrice)
                .expectedPrice(cartPrice)
                .customizationOptions(testData.getCustomizationOptions())
                .build();

        String jsonFilePath = saveTestDataToJson(customizationData, "customization");
        attachJsonData(customizationData, "Customization Price Data");

        // Validation
        Allure.step("Base Price: " + searchResultsPrice);
        Allure.step("Customized Price: " + customizedPrice);
        Allure.step("Cart Price: " + cartPrice);



//        Allure.step("Product Item has been removed");
//
//        AssertionUtils.assertTrue(
//                shoppingCartPage.isKeyWordHasBeenRemovedDisplayed(),
//                String.format("Item removal confirmation should be displayed on browser: %s", currentBrowser));
//
//        attachScreenshot(String.format("Removal confirmation dialog displays 'Item has been removed' - %s", currentBrowser));

        Allure.step("Price in shopping cart: " + cartPrice);

        AssertionUtils.assertTrue(
                shoppingCartPage.isCartIconItemCountDisplayed(),
                String.format("Cart icon should display '1' after adding a product on browser: %s", currentBrowser));

        attachScreenshot(String.format("Cart icon displays 1 after adding an item - %s", currentBrowser));

        removeItem();

        log.info("Customization validation completed. Data saved to: {}", jsonFilePath);
    }

    @Test(dataProvider = "searchDataProvider", dataProviderClass = TestDataUtils.class,
            groups = {"regression", "cart"})
    @Story("Price Validation with Product Customization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify customized product price changes and capture all price variations")
    @Issue("BALSAM-004")
    public void testValidateItemRemovalConfirmationMessage(SearchTestData testData)
            throws InterruptedException {

        addTestParametersToReport(testData);
        log.step("Starting customization price validation for browser: {}", currentBrowser);

        performLogin();
        performSearch(testData.getSearchTerm());

        // Capture base price before customization
        String searchResultsPrice = searchResultsPage.selectProductAndGetPrice(testData.getProductIndex());

        // Apply customizations
        applyCustomizations(testData.getCustomizationOptions());

        // Capture price after customization
        String customizedPrice = productPage.getDisplayedProductPrice();

        addProductToCart();
        navigateToCart();

        String cartPrice = shoppingCartPage.getDisplayedProductPrice();

        // Create detailed customization test data
        SearchTestData customizationData = new SearchTestData.Builder()
                .searchTerm(testData.getSearchTerm())
                .productIndex(testData.getProductIndex())
                .currentPrice(searchResultsPrice)
                .newPrice(customizedPrice)
                .expectedPrice(cartPrice)
                .customizationOptions(testData.getCustomizationOptions())
                .build();

        String jsonFilePath = saveTestDataToJson(customizationData, "customization");
        attachJsonData(customizationData, "Customization Price Data");

        // Validation
        Allure.step("Base Price: " + searchResultsPrice);
        Allure.step("Customized Price: " + customizedPrice);
        Allure.step("Cart Price: " + cartPrice);

        removeItem();

        Allure.step("Product Item has been removed");

        AssertionUtils.assertTrue(
                shoppingCartPage.isKeyWordHasBeenRemovedDisplayed(),
                String.format("Item removal confirmation should be displayed on browser: %s", currentBrowser));

        attachScreenshot(String.format("Removal confirmation dialog displays 'Item has been removed' - %s", currentBrowser));

        log.info("Customization validation completed. Data saved to: {}", jsonFilePath);
    }



    // Helper Methods
    private void initializePageObjects(WebDriver driver) {
        if (driver == null) {
            throw new RuntimeException("WebDriver is null - cannot initialize page objects");
        }

        loginPage = new LoginPage(driver);
        myAccountPage = new MyAccountPage(driver);
        searchResultsPage = new SearchResultsPage(driver);
        productPage = new ProductPage(driver);
        productDetailsModal = new ProductDetailsModal(driver);
        shoppingCartPage = new ShoppingCartPage(driver);

        log.info("Page objects initialized successfully for browser: {}", currentBrowser);
    }

    private void addTestParametersToReport(SearchTestData testData) {
        Allure.parameter("Search Term", testData.getSearchTerm());
        Allure.parameter("Expected Price", testData.getCurrentPrice());
        Allure.parameter("Product Index", testData.getProductIndex());
        Allure.parameter("Browser", currentBrowser);

        if (testData.getCustomizationOptions() != null) {
            Allure.parameter("Customization Options", testData.getCustomizationOptions().toString());
        }
    }

    private SearchTestData createEnhancedTestData(SearchTestData originalData, String actualPrice) {
        return new SearchTestData.Builder()
                .searchTerm(originalData.getSearchTerm())
                .productIndex(originalData.getProductIndex())
                .currentPrice(actualPrice)
                .expectedPrice(originalData.getCurrentPrice())
                .expectedItemName(originalData.getExpectedItemName())
                .customizationOptions(originalData.getCustomizationOptions())
                .build();
    }

    private String saveTestDataToJson(SearchTestData testData, String testType) {
        try {
            String fileName = String.format("%s_%s_%s_%s.json",
                    testType,
                    currentBrowser,
                    testData.getSearchTerm().replaceAll("\\s+", "_"),
                    testStartTime);

            String filePath = JSON_OUTPUT_DIR + fileName;
            testData.saveToJsonFile(filePath);

            Allure.step("Test data saved to JSON: " + filePath);
            return filePath;

        } catch (IOException e) {
            log.error("Failed to save test data to JSON: {}", e.getMessage());
            Allure.step("Failed to save JSON data: " + e.getMessage());
            return "Failed to save";
        }
    }

    // Action Methods with Allure Steps
    @Step("Perform user login")
    private void performLogin() {
        try {
            loginPage.login();
            Allure.step(String.format("User logged in successfully on browser: %s", currentBrowser));
        } catch (InterruptedException e) {
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    @Step("Search for product: {searchTerm}")
    private void performSearch(String searchTerm) {
        myAccountPage.search(searchTerm);
        Allure.step(String.format("Search performed for: %s on browser: %s", searchTerm, currentBrowser));
    }

//    @Step("Select product at index: {productIndex}")
//    private String selectProductAndGetPrice(int productIndex) {
//        String actualPrice = searchResultsPage.selectProductAndGetPrice(testData.getProductIndex());
//        Allure.step(String.format("Product selected at index: %d on browser: %s", productIndex, currentBrowser));
//        return actualPrice;
//    }

    @Step("Apply product customizations")
    private void applyCustomizations(Object customizationOptions) {
        if (customizationOptions instanceof Map) {
            productPage.selectOptions((Map<String, String>) customizationOptions);
            Allure.step(String.format("Customization options applied: %s on browser: %s",
                    customizationOptions.toString(), currentBrowser));
        }
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
    private void removeItem() {
        shoppingCartPage.deleteItem();
        Allure.step(String.format("Item removed from cart on browser: %s", currentBrowser));
    }

    // Attachment Methods
    @Attachment(value = "{name}", type = "image/png")
    private void attachScreenshot(String name) {
        WebDriver driver = DriverManager.getDriver();

        if (driver == null) {
            log.error("WebDriver is null when trying to capture screenshot: {} on browser: {}", name, currentBrowser);
            return;
        }

        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            AllureReportManager.attachScreenshot(name + " - " + currentBrowser, screenshot);
            log.info("Screenshot captured: {} on browser: {}", name, currentBrowser);
        } catch (Exception e) {
            log.error("Failed to capture screenshot: {} on browser: {} due to {}", name, currentBrowser, e.getMessage());
        }
    }

    @Attachment(value = "{attachmentName}", type = "application/json")
    private String attachJsonData(SearchTestData testData, String attachmentName) {
        try {
            return objectMapper.writeValueAsString(testData);
        } catch (Exception e) {
            log.error("Failed to serialize test data for attachment: {}", e.getMessage());
            return String.format("Failed to serialize test data: %s", e.getMessage());
        }
    }

    @Attachment(value = "Browser Console Logs", type = "text/plain")
    private String attachBrowserLogs() {
        WebDriver driver = DriverManager.getDriver();
        if (driver != null) {
            try {
                // Implementation to capture browser logs if needed
                return String.format("Browser: %s, Console logs captured at %s", currentBrowser, testStartTime);
            } catch (Exception e) {
                return String.format("Failed to capture browser logs: %s", e.getMessage());
            }
        }
        return "WebDriver not available for log capture";
    }
}