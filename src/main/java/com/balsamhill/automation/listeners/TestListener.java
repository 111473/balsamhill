package com.balsamhill.automation.listeners;

import com.balsamhill.automation.reports.AllureReportManager;
import org.testng.*;

public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        AllureReportManager.attachText("Test Failure", "Failed test: " + result.getName());

        try {
            Object testInstance = result.getInstance();
            Class<?> clazz = testInstance.getClass().getSuperclass();

            if (clazz.getDeclaredField("driver") != null) {
                // Selenium WebDriver
                Object driverObj = clazz.getDeclaredField("driver").get(testInstance);
                if (driverObj instanceof org.openqa.selenium.TakesScreenshot) {
                    byte[] screenshot = ((org.openqa.selenium.TakesScreenshot) driverObj)
                            .getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
                    AllureReportManager.attachScreenshot("Selenium Failure - " + result.getName(), screenshot);
                }
            }
        } catch (Exception e) {
            AllureReportManager.attachText("Listener Exception", e.getMessage());
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        AllureReportManager.attachText("Test Start", "Starting test: " + result.getName());
    }
}
