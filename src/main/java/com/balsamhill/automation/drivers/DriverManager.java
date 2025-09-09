package com.balsamhill.automation.drivers;
import org.openqa.selenium.WebDriver;


public class DriverManager {

    private static final ThreadLocal<WebDriver> WEB_DRIVER_THREAD_LOCAL = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return WEB_DRIVER_THREAD_LOCAL.get();
    }

    public static void setDriver(WebDriver driver) {
        WEB_DRIVER_THREAD_LOCAL.set(driver);
    }

    /**
     * Remove and quit the WebDriver instance for the current thread.
     * This method ensures that the WebDriver is properly closed and resources are released.
     */
    public static void removeDriver() {
        try {
            if (WEB_DRIVER_THREAD_LOCAL.get() != null) {
                WEB_DRIVER_THREAD_LOCAL.get().quit();
            }
        } catch (Exception e) {
            // Optionally log error
        } finally {
            WEB_DRIVER_THREAD_LOCAL.remove();
        }
    }

    /**
     * Clear the WebDriver instance for the current thread.
     * This method is a convenience wrapper around removeDriver().
     */
    public static void clearDriver() {
        removeDriver();
    }
}
