package com.balsamhill.automation.utils;

import com.balsamhill.automation.logger.LoggerWrapper;
import org.testng.Assert;


public class AssertionUtils {

    private static final LoggerWrapper log = new LoggerWrapper(AssertionUtils.class);

    public static void assertTrue(boolean condition, String message) {
        log.step("Assertion [TRUE]: {}", message);
        Assert.assertTrue(condition, message);
    }

    public static void assertFalse(boolean condition, String message) {
        log.step("Assertion [FALSE]: {}", message);
        Assert.assertFalse(condition, message);
    }

    public static void assertEquals(String actual, String expected, String message) {
        log.step("Assertion [EQUALS]: expected='{}', actual='{}'", expected, actual);
        Assert.assertEquals(actual, expected, message);
    }

    public static void assertContains(String actual, String expectedSubstring, String message) {
        log.step("Assertion [CONTAINS]: '{}' contains '{}'", actual, expectedSubstring);
        Assert.assertTrue(actual.contains(expectedSubstring), message);
    }
}
