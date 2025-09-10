package com.balsamhill.automation.runners;

import com.balsamhill.automation.tests.CrossBrowserSearchTests;
import org.testng.TestNG;

public class TestRunner {
    public static void main(String[] args) {
        TestNG testng = new TestNG();

         testng.setTestClasses(new Class[] {
                 com.balsamhill.automation.tests.BalsamHillSearchTests.class
         });

        testng.run();
    }
}