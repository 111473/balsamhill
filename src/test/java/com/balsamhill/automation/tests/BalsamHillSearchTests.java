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

    @BeforeMethod
    @Step("Set up test environment and initialize page objects")
    public void setUp() {
        super.setUp(); // This calls BaseTest.setUp() which calls DriverFactory.initDriver()

        // Get the driver from DriverManager
        WebDriver driver = DriverManager.getDriver();

        // Debug log to verify driver is not null
        log.info("Driver from DriverManager: " + driver);
        Allure.addAttachment("Driver Info", "Driver initialized: " + (driver != null ? "Success" : "Failed"));

        // Initialize page objects with the driver
        loginPage = new LoginPage(driver);
        myAccountPage = new MyAccountPage(driver);
        searchResultsPage = new SearchResultsPage(driver);
        productPage = new ProductPage(driver);
        productDetailsModal = new ProductDetailsModal(driver);
        shoppingCartPage = new ShoppingCartPage(driver);

        log.info("Page objects initialized successfully");
        Allure.step("Page objects initialized successfully");
    }

    @Test(dataProvider = "searchDataProvider", dataProviderClass = TestDataUtils.class)
    @Story("Price Validation on Search Results")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that product price on search results page matches the expected price from test data without any customization")
    @Issue("BALSAM-001")
    public void testPriceOnSearchResultsMatchesTestDataWithoutProductCustomization(SearchTestData testData)
            throws InterruptedException {

        Allure.parameter("Search Term", testData.getSearchTerm());
        Allure.parameter("Expected Price", testData.getCurrentPrice());
        Allure.parameter("Product Index", testData.getProductIndex());

        performLogin();
        performSearch(testData.getSearchTerm());

        String expectedPrice = searchResultsPage.fetchBasePriceFromJson(testData.getCurrentPrice());
        String actualPrice = searchResultsPage.getDisplayedProductPrice();

        Allure.step("Expected Price: " + expectedPrice);
        Allure.step("Actual Price: " + actualPrice);

        AssertionUtils.assertEquals(
                expectedPrice,
                actualPrice,
                "Verification confirmed that the product base price from the Search Results Page matched the price in the Test Data."
        );

        attachScreenshot("Validate Price on Search Result Page without Product Customization");
        Allure.step("Price validation completed successfully on Search Results page");
    }

    @Test(dataProvider = "searchDataProvider", dataProviderClass = TestDataUtils.class)
    @Story("Price Validation on Product Details")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that product price on product details page matches the expected price from test data without any customization")
    @Issue("BALSAM-002")
    public void testPriceOnProductDetailsMatchesTestDataWithoutProductCustomization(SearchTestData testData)
            throws InterruptedException {

        Allure.parameter("Search Term", testData.getSearchTerm());
        Allure.parameter("Expected Price", testData.getCurrentPrice());
        Allure.parameter("Product Index", testData.getProductIndex());

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
                "Verified: Product base price from the Search Results Page matches the price in the Test Data."
        );

        attachScreenshot("Validate Price on Product Details Page without Product Customization");
        Allure.step("Price validation completed successfully on Product Details page");
    }

    @Test(dataProvider = "searchDataProvider", dataProviderClass = TestDataUtils.class)
    @Story("Price Validation on Shopping Cart")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that product price in shopping cart matches the expected price from test data without any customization")
    @Issue("BALSAM-003")
    public void testPriceOnShoppingCartMatchesTestDataWithoutProductCustomization(SearchTestData testData)
            throws InterruptedException {

        Allure.parameter("Search Term", testData.getSearchTerm());
        Allure.parameter("Expected Price", testData.getCurrentPrice());
        Allure.parameter("Product Index", testData.getProductIndex());

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
                "Verified: Product base price from the Product Page matches the price in the Test Data."
        );

        attachScreenshot("Validate Price on Shopping Cart Page without Product Customization");
        Allure.step("Price validation completed successfully on Shopping Cart page");
    }

    @Test(dataProvider = "searchDataProvider", dataProviderClass = TestDataUtils.class)
    @Story("Price Validation with Product Customization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that customized product price in shopping cart matches the adjusted price after customization options are selected")
    @Issue("BALSAM-004")
    public void testPriceOnShoppingCartMatchesTestDataWithProductCustomization(SearchTestData testData)
            throws InterruptedException {

        Allure.parameter("Search Term", testData.getSearchTerm());
        Allure.parameter("Base Price", testData.getCurrentPrice());
        Allure.parameter("Product Index", testData.getProductIndex());
        Allure.parameter("Customization Options", testData.getCustomizationOptions().toString());

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
                "Verified: Product base price from the Search Results Page matches the price in the Test Data."
        );

        attachScreenshot("Validate Price on Shopping Cart Page with Product Customization");
        Allure.step("Price validation with customization completed successfully");
    }

    // Helper methods with Allure steps
    @Step("Perform user login")
    private void performLogin() {
        try {
            loginPage.login();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Allure.step("User logged in successfully");
    }

    @Step("Search for product: {searchTerm}")
    private void performSearch(String searchTerm) {
        myAccountPage.search(searchTerm);
        Allure.step("Search performed for: " + searchTerm);
    }

    @Step("Select product at index: {productIndex}")
    private void selectProduct(int productIndex) {
        searchResultsPage.selectProduct(productIndex);
        Allure.step("Product selected at index: " + productIndex);
    }

    @Step("Apply product customizations")
    private void applyCustomizations(Object customizationOptions) {
        productPage.selectOptions((Map<String, String>) customizationOptions);
        Allure.step("Customization options applied: " + customizationOptions.toString());
    }

    @Step("Add product to shopping cart")
    private void addProductToCart() {
        productPage.addToCart();
        Allure.step("Product added to cart successfully");
    }

    @Step("Navigate to shopping cart")
    private void navigateToCart() {
        productDetailsModal.viewCart();
        Allure.step("Navigated to shopping cart");
    }

    @Attachment(value = "{name}", type = "image/png")
    private void attachScreenshot(String name) {
        WebDriver driver = DriverManager.getDriver();

        if (driver == null) {
            log.error("WebDriver is null when trying to capture screenshot: " + name);
            Allure.step("Failed to capture screenshot - WebDriver is null");
            return;
        }

        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            AllureReportManager.attachScreenshot(name, screenshot);
            log.info("Screenshot captured: " + name);
            Allure.step("Screenshot captured: " + name);
        } catch (Exception e) {
            log.error("Failed to capture screenshot: " + name + " due to " + e.getMessage());
            Allure.step("Failed to capture screenshot: " + e.getMessage());
        }
    }

    @Attachment(value = "Test Data", type = "application/json")
    public String attachTestData(SearchTestData testData) {
        // You can serialize your test data to JSON and attach it
        return testData.toString(); // Replace with proper JSON serialization
    }

    @Attachment(value = "Browser Console Logs", type = "text/plain")
    public String attachBrowserLogs() {
        // Implementation to capture and attach browser console logs
        WebDriver driver = DriverManager.getDriver();
        // Add logic to capture browser logs if needed
        return "Browser logs captured";
    }
}