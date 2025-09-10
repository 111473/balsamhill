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
import org.openqa.selenium.firefox.FirefoxOptions;

public class DriverFactory {

    private static final LoggerWrapper log = new LoggerWrapper(DriverFactory.class);

    private static final String FIREFOX = "firefox";
    private static final String EDGE = "edge";
    private static final String CHROME = "chrome";

    /**
     * Initializes the WebDriver based on configuration settings or TestNG parameters.
     * Supports Chrome, Firefox, and Edge browsers with optional headless mode.
     * Ensures thread-safe WebDriver instances using DriverManager.
     *
     * @param browserName Optional browser parameter from TestNG, overrides config if provided
     */
    public static void initDriver(String... browserName) {
        if (DriverManager.getDriver() != null) {
            log.info("Selenium WebDriver already initialized for this thread.");
            return;
        }

        log.info("Thread {} - Driver initialized: {}",
                Thread.currentThread().getId(), DriverManager.getDriver());

        // Use TestNG parameter if provided, otherwise fall back to config
        String browser = (browserName.length > 0 && browserName[0] != null) ?
                browserName[0].toLowerCase() :
                ConfigManager.get("browser").toLowerCase();

        String baseUrl = ConfigManager.get("baseUrl");
        boolean isHeadless = ConfigManager.getBoolean("headless");

        WebDriver driver = createDriver(browser, isHeadless);
        DriverManager.setDriver(driver);

        driver.get(baseUrl);
        log.step("Navigated to base URL: {} using browser: {}", baseUrl, browser);

        driver.manage().window().maximize();
        log.step("Browser window maximized for: {}", browser);
    }

    /**
     * Creates a WebDriver instance based on the specified browser type.
     *
     * @param browser    The browser type (chrome, firefox, edge)
     * @param isHeadless Whether to run in headless mode
     * @return WebDriver instance
     */
    private static WebDriver createDriver(String browser, boolean isHeadless) {
        WebDriver driver;

        log.info("Initializing Selenium WebDriver for browser: {}", browser);

        switch (browser) {
            case FIREFOX:
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = getFirefoxOptions(isHeadless);
                driver = new FirefoxDriver(firefoxOptions);
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

        return driver;
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
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-extensions");

        if (isHeadless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-sandbox");
            options.addArguments("--window-size=1920,1080");
            log.info("Chrome headless mode enabled.");
        }

        return options;
    }

    /**
     * Configures EdgeOptions based on headless mode setting.
     *
     * @param isHeadless true to enable headless mode, false otherwise
     * @return Configured EdgeOptions instance
     */
    private static EdgeOptions getEdgeOptions(boolean isHeadless) {
        EdgeOptions options = new EdgeOptions();
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-extensions");

        if (isHeadless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-sandbox");
            options.addArguments("--window-size=1920,1080");
            log.info("Edge headless mode enabled.");
        }

        return options;
    }

    /**
     * Configures FirefoxOptions based on headless mode setting.
     *
     * @param isHeadless true to enable headless mode, false otherwise
     * @return Configured FirefoxOptions instance
     */
    private static FirefoxOptions getFirefoxOptions(boolean isHeadless) {
        FirefoxOptions options = new FirefoxOptions();

        if (isHeadless) {
            options.addArguments("--headless");
            options.addArguments("--width=1920");
            options.addArguments("--height=1080");
            log.info("Firefox headless mode enabled.");
        }

        // Additional Firefox-specific configurations
        options.addPreference("dom.webnotifications.enabled", false);
        options.addPreference("media.volume_scale", "0.0");

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
        // Cleanup for thread-local instances
        DriverManager.clearDriver();
        log.info("Selenium WebDriver resources cleared for this thread.");
    }
}