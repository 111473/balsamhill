package com.balsamhill.automation.drivers;

import com.balsamhill.automation.logger.LoggerWrapper;
import com.balsamhill.automation.utils.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;


public class DriverFactory {

    private static final LoggerWrapper log = new LoggerWrapper(DriverFactory.class);

    private static final String FIREFOX = "firefox";
    private static final String EDGE = "edge";
    private static final String CHROME = "chrome";

    /**
     * Initializes the WebDriver based on configuration settings.
     * Supports Chrome, Firefox, and Edge browsers with optional headless mode.
     * Ensures thread-safe WebDriver instances using DriverManager.
     */
    public static void initDriver() {
        if (DriverManager.getDriver() != null) {
            log.info("Selenium WebDriver already initialized for this thread.");
            return;
        }

        String browser = ConfigManager.get("browser").toLowerCase();
        String baseUrl = ConfigManager.get("baseUrl");
        boolean isHeadless = ConfigManager.getBoolean("headless");

        WebDriver driver;

        log.info("Initializing Selenium WebDriver for browser: {}", browser);

        switch (browser) {
            case FIREFOX:
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver();
                log.info("FirefoxDriver initialized.");
                break;

            case EDGE:
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = getEdgeOptions(isHeadless);
                driver = new EdgeDriver(edgeOptions);
                log.info("EdgeDriver initialized.");
                break;

            case CHROME:
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = getChromeOptions(isHeadless);
                driver = new ChromeDriver(chromeOptions);
                log.info("ChromeDriver initialized.");
                break;
        }

        DriverManager.setDriver(driver);

        driver.get(baseUrl);
        log.step("Navigated to base URL: {}", baseUrl);

        driver.manage().window().maximize();
        log.step("Browser window maximized.");
    }

    /**
     * Configures ChromeOptions based on headless mode setting.
     *
     * @param isHeadless true to enable headless mode, false otherwise
     * @return Configured ChromeOptions instance
     */
    private static ChromeOptions getChromeOptions(boolean isHeadless) {
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--remote-allow-origins=*");

        if (isHeadless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            log.info("Chrome headless mode enabled.");
        }

        return options;
    }

    /**
     * Configures ChromeOptions based on headless mode setting.
     *
     * @param isHeadless true to enable headless mode, false otherwise
     * @return Configured ChromeOptions instance
     */
    private static EdgeOptions getEdgeOptions(boolean isHeadless) {
        EdgeOptions options = new EdgeOptions();
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--remote-allow-origins=*");

        if (isHeadless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            log.info("Edge headless mode enabled.");
        }

        return options;
    }


    /**
     * Closes and quits the WebDriver instance for the current thread.
     * Ensures proper cleanup of resources and handles exceptions during quit.
     */
    public static void closeDriver() {
        WebDriver driver = DriverManager.getDriver();
        if (driver != null) {
            try {
                driver.quit();
                log.info("WebDriver quit successfully.");
            } catch (Exception e) {
                log.error("Failed to quit WebDriver: {}", e.getMessage());
            }
        }
        //cleanup for thread-local instances
        DriverManager.clearDriver();
        log.info("Selenium WebDriver resources cleared for this thread.");
    }
}

