package com.balsamhill.automation.tests;

import com.balsamhill.automation.base.BaseTest;
import com.balsamhill.automation.drivers.DriverManager;
import com.balsamhill.automation.logger.LoggerWrapper;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CrossBrowserSearchTests extends BaseTest {

    private static final LoggerWrapper log = new LoggerWrapper(CrossBrowserSearchTests.class);

    /**
     * Data provider for cross-browser testing
     * Returns different browser configurations
     */
    @DataProvider(name = "browserProvider", parallel = true)
    public Object[][] getBrowsers() {
        return new Object[][] {
                {"chrome"},
                {"edge"},
                {"firefox"}
        };
    }

//    /**
//     * Cross-browser search test using DataProvider
//     * This approach runs the same test across multiple browsers
//     */
//    @Test(dataProvider = "browserProvider")
//    public void testSearchFunctionalityAcrossBrowsers(String browser) {
//        // Initialize driver with specific browser
//        setUp(browser);
//
//        try {
//            WebDriver driver = DriverManager.getDriver();
//            log.step("Starting search test on browser: {}", browser);
//
//            // Verify page title
//            String title = driver.getTitle();
//            log.info("Page title on {}: {}", browser, title);
//            Assert.assertTrue(title.contains("Balsam Hill"),
//                    "Page title should contain 'Balsam Hill' on " + browser);
//
//            // Add your search test logic here
//            performSearchTest(driver, browser);
//
//            log.step("Search test completed successfully on browser: {}", browser);
//
//        } catch (Exception e) {
//            log.error("Test failed on browser {}: {}", browser, e.getMessage());
//            Assert.fail("Test failed on " + browser + ": " + e.getMessage());
//        } finally {
//            tearDown();
//        }
//    }
//
//    /**
//     * Parameterized test method that accepts browser from TestNG XML
//     * This approach is used when browser is passed as parameter in TestNG suite
//     */
//    @Test
//    public void testSearchWithParameterizedBrowser() {
//        WebDriver driver = DriverManager.getDriver();
//        String currentUrl = driver.getCurrentUrl();
//
//        log.step("Running search test on current browser");
//        log.info("Current URL: {}", currentUrl);
//
//        // Verify we're on the correct page
//        Assert.assertTrue(currentUrl.contains("balsamhill.com"),
//                "Should be on Balsam Hill website");
//
//        // Add your search test logic here
//        performSearchTest(driver, "current browser");
//
//        log.step("Parameterized search test completed successfully");
//    }
//
//    /**
//     * Helper method to perform actual search test logic
//     * This keeps the test logic DRY across different test methods
//     */
//    private void performSearchTest(WebDriver driver, String browserInfo) {
//        log.step("Performing search test logic on {}", browserInfo);
//
//        // Example search test steps:
//        // 1. Find search element
//        // 2. Enter search term
//        // 3. Submit search
//        // 4. Verify results
//
//        // Add your actual search test implementation here
//        // For example:
//        /*
//        WebElement searchBox = driver.findElement(By.id("search"));
//        searchBox.sendKeys("Christmas Tree");
//        searchBox.submit();
//
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//        List<WebElement> results = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
//            By.cssSelector(".search-results .product")));
//
//        Assert.assertTrue(results.size() > 0, "Search should return results");
//        log.step("Found {} search results on {}", results.size(), browserInfo);
//        */
//
//        log.info("Search test logic executed on {}", browserInfo);
//    }
//
//    /**
//     * Override setUp to accept browser parameter for DataProvider tests
//     */
//    public void setUp(String browser) {
//        com.balsamhill.automation.drivers.DriverFactory.initDriver(browser);
//        log.step("Selenium WebDriver initialized for browser: {}", browser);
//    }
}