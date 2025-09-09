package com.balsamhill.automation.base;

import com.balsamhill.automation.drivers.DriverFactory;
import com.balsamhill.automation.logger.LoggerWrapper;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {

    private static final LoggerWrapper log = new LoggerWrapper(BaseTest.class);

    @BeforeMethod
    public void setUp() {
        DriverFactory.initDriver();
        log.step("Selenium WebDriver initialized using SeleniumDriverFactory.");
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.closeDriver();
        log.step("Selenium WebDriver closed and cleared via SeleniumDriverFactory.");
    }
}
