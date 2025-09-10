package com.balsamhill.automation.base;

import com.balsamhill.automation.drivers.DriverFactory;
import com.balsamhill.automation.logger.LoggerWrapper;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

public abstract class BaseTest {

    private static final LoggerWrapper log = new LoggerWrapper(BaseTest.class);

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        setUp("chrome", "staging"); // Call the parameterized version with defaults
    }


    /**
     * Setup method that initializes WebDriver based on TestNG parameters
     * Browser parameter comes from TestNG suite XML
     * This method can be overridden by child classes
     */
    @Parameters({"browser", "environment"})
    public void setUp(@Optional("chrome") String browser, @Optional("staging") String environment) {
        log.info("Setting up test with browser: {} and environment: {}", browser, environment);

        // Initialize driver with browser parameter from TestNG
        DriverFactory.initDriver(browser);

        log.step("Test setup completed for browser: {}", browser);
    }

    /**
     * Cleanup method that closes WebDriver after each test method
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        log.step("Starting test cleanup...");
        DriverFactory.closeDriver();
        log.step("Test cleanup completed - WebDriver closed and resources cleared");
    }

    /**
     * Protected method for initializing driver - can be called by child classes
     */
    protected void initializeDriver(String browser) {
        DriverFactory.initDriver(browser);
        log.step("Driver initialized for browser: {}", browser);
    }
}